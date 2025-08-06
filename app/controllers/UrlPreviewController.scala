package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import java.net.{URL, URLDecoder}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.duration.DurationInt

case class UrlPreviewData(
    url: String,
    title: Option[String] = None,
    description: Option[String] = None,
    image: Option[String] = None,
    siteName: Option[String] = None,
    favicon: Option[String] = None
)

object UrlPreviewData {
    implicit val writes: Writes[UrlPreviewData] = Json.writes[UrlPreviewData]
}

@Singleton
class UrlPreviewController @Inject()(
    val controllerComponents: ControllerComponents,
    ws: WSClient
)(implicit ec: ExecutionContext) extends BaseController {

    def preview(): Action[AnyContent] = Action.async { implicit request =>
        val urlParam = request.getQueryString("url")

        urlParam match {
            case Some(encodedUrl) =>
                val url = URLDecoder.decode(encodedUrl, "UTF-8")

                if (isValidUrl(url)) {
                    fetchUrlPreview(url).map { previewData =>
                        Ok(Json.toJson(previewData)).withHeaders(
                            "Access-Control-Allow-Origin" -> "*",
                            "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS",
                            "Access-Control-Allow-Headers" -> "Content-Type, Authorization"
                        )
                    }.recover {
                        case ex: Exception =>
                            BadRequest(Json.obj(
                                "error" -> "Failed to fetch URL preview",
                                "message" -> ex.getMessage
                            )).withHeaders(
                                "Access-Control-Allow-Origin" -> "*"
                            )
                    }
                } else {
                    Future.successful(
                        BadRequest(Json.obj("error" -> "Invalid URL")).withHeaders(
                            "Access-Control-Allow-Origin" -> "*"
                        )
                    )
                }

            case None =>
                Future.successful(
                    BadRequest(Json.obj("error" -> "URL parameter is required")).withHeaders(
                        "Access-Control-Allow-Origin" -> "*"
                    )
                )
        }
    }

    // Handle CORS preflight requests
    def previewOptions(): Action[AnyContent] = Action { implicit request =>
        Ok("").withHeaders(
            "Access-Control-Allow-Origin" -> "*",
            "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS",
            "Access-Control-Allow-Headers" -> "Content-Type, Authorization"
        )
    }

    private def fetchUrlPreview(url: String): Future[UrlPreviewData] = {
        // Try multiple strategies to get preview data
        fetchFromJsonLink(url).recoverWith {
            case _ => fetchFromDirectScraping(url)
        }.recoverWith {
            case _ => Future.successful(createFallbackPreview(url))
        }
    }

    private def fetchFromJsonLink(url: String): Future[UrlPreviewData] = {
        val apiUrl = s"https://jsonlink.io/api/extract?url=${java.net.URLEncoder.encode(url, "UTF-8")}"

        ws.url(apiUrl)
            .withRequestTimeout(10.seconds) // 10 seconds timeout
            .get()
            .map { response =>
                if (response.status == 200) {
                    val json = response.json
                    UrlPreviewData(
                        url = url,
                        title = (json \ "title").asOpt[String].filter(_.nonEmpty),
                        description = (json \ "description").asOpt[String].filter(_.nonEmpty),
                        image = (json \ "images").asOpt[Seq[String]].flatMap(_.headOption)
                            .orElse((json \ "image").asOpt[String]).filter(_.nonEmpty),
                        siteName = (json \ "domain").asOpt[String].filter(_.nonEmpty)
                            .orElse(Some(new URL(url).getHost)),
                        favicon = (json \ "favicon").asOpt[String].filter(_.nonEmpty)
                            .orElse(Some(s"https://www.google.com/s2/favicons?domain=${new URL(url).getHost}&sz=32"))
                    )
                } else {
                    throw new Exception(s"API returned status: ${response.status}")
                }
            }
    }

    private def fetchFromDirectScraping(url: String): Future[UrlPreviewData] = {
        ws.url(url)
            .withRequestTimeout(10.seconds)
            .withFollowRedirects(true)
            .get()
            .map { response =>
                if (response.status == 200) {
                    parseHtmlForPreview(url, response.body)
                } else {
                    throw new Exception(s"Failed to fetch URL: ${response.status}")
                }
            }
    }

    private def parseHtmlForPreview(url: String, html: String): UrlPreviewData = {
        val doc: Document = Jsoup.parse(html)
        val urlObj = new URL(url)

        val title = Option(doc.select("meta[property=og:title]").attr("content"))
            .filter(_.nonEmpty)
            .orElse(Option(doc.select("meta[name=twitter:title]").attr("content")).filter(_.nonEmpty))
            .orElse(Option(doc.title()).filter(_.nonEmpty))

        val description = Option(doc.select("meta[property=og:description]").attr("content"))
            .filter(_.nonEmpty)
            .orElse(Option(doc.select("meta[name=twitter:description]").attr("content")).filter(_.nonEmpty))
            .orElse(Option(doc.select("meta[name=description]").attr("content")).filter(_.nonEmpty))

        val image = Option(doc.select("meta[property=og:image]").attr("content"))
            .filter(_.nonEmpty)
            .orElse(Option(doc.select("meta[name=twitter:image]").attr("content")).filter(_.nonEmpty))
            .map(img => if (img.startsWith("http")) img else s"${urlObj.getProtocol}://${urlObj.getHost}$img")

        val siteName = Option(doc.select("meta[property=og:site_name]").attr("content"))
            .filter(_.nonEmpty)
            .orElse(Some(urlObj.getHost))

        val favicon = Option(doc.select("link[rel=icon]").attr("href"))
            .orElse(Option(doc.select("link[rel='shortcut icon']").attr("href")))
            .map(fav => if (fav.startsWith("http")) fav else s"${urlObj.getProtocol}://${urlObj.getHost}$fav")
            .orElse(Some(s"https://www.google.com/s2/favicons?domain=${urlObj.getHost}&sz=32"))

        UrlPreviewData(
            url = url,
            title = title,
            description = description,
            image = image,
            siteName = siteName,
            favicon = favicon
        )
    }

    private def createFallbackPreview(url: String): UrlPreviewData = {
        val urlObj = new URL(url)
        UrlPreviewData(
            url = url,
            title = Some(urlObj.getHost),
            description = Some("Click to visit this website"),
            siteName = Some(urlObj.getHost),
            favicon = Some(s"https://www.google.com/s2/favicons?domain=${urlObj.getHost}&sz=32")
        )
    }

    private def isValidUrl(url: String): Boolean = {
        Try(new URL(url)) match {
            case Success(_) => true
            case Failure(_) => false
        }
    }
}