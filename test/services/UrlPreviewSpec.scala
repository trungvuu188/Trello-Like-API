package services

import controllers.UrlPreviewData
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Json}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class UrlPreviewServiceSpec
    extends AsyncWordSpec
        with Matchers
        with MockitoSugar
        with ScalaFutures {

    implicit val ec: ExecutionContext = ExecutionContext.global

    val mockWsClient: WSClient = mock[WSClient]
    val mockJsonlinkRequest: WSRequest = mock[WSRequest]
    val mockOgRequest: WSRequest = mock[WSRequest]
    val mockResponse: WSResponse = mock[WSResponse]
    val mockOgResponse: WSResponse = mock[WSResponse]

    val service = new UrlPreviewService(mockWsClient)

    val testUrl = "https://example.com"

    "UrlPreviewService#fetchPreview" should {

        "return preview data from JsonLink API when it returns 200" in {
            val json = Json.obj(
                "title" -> "Example Title",
                "description" -> "Example Description",
                "images" -> Json.arr("https://example.com/image.jpg"),
                "domain" -> "example.com",
                "favicon" -> "https://example.com/favicon.ico"
            )

            when(mockWsClient.url(org.mockito.ArgumentMatchers.contains("jsonlink.io")))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.withRequestTimeout(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.get())
                .thenReturn(Future.successful(mockResponse))

            when(mockResponse.status).thenReturn(200)
            when(mockResponse.json).thenReturn(json)

            service.fetchPreview(testUrl).map { result =>
                result.title mustBe Some("Example Title")
                result.description mustBe Some("Example Description")
                result.image mustBe Some("https://example.com/image.jpg")
                result.siteName mustBe Some("example.com")
                result.favicon mustBe Some("https://example.com/favicon.ico")
            }
        }

        "fall back to OpenGraph when JsonLink API fails" in {
            // JsonLink fails
            when(mockWsClient.url(org.mockito.ArgumentMatchers.contains("jsonlink.io")))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.withRequestTimeout(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.get())
                .thenReturn(Future.failed(new RuntimeException("JsonLink failed")))

            // OpenGraph succeeds
            when(mockWsClient.url(org.mockito.ArgumentMatchers.eq(testUrl)))
                .thenReturn(mockOgRequest)
            when(mockOgRequest.withRequestTimeout(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockOgRequest)
            when(mockOgRequest.withFollowRedirects(true))
                .thenReturn(mockOgRequest)
            when(mockOgRequest.get())
                .thenReturn(Future.successful(mockOgResponse))

            val html =
                """
                  |<html>
                  |<head>
                  |<title>Fallback Title</title>
                  |<meta property="og:description" content="Fallback description"/>
                  |</head>
                  |<body></body>
                  |</html>
                  |""".stripMargin

            when(mockOgResponse.status).thenReturn(200)
            when(mockOgResponse.body).thenReturn(html)

            service.fetchPreview(testUrl).map { result =>
                result.title mustBe Some("Fallback Title")
                result.description mustBe Some("Fallback description")
            }
        }

        "return basic preview when all sources fail" in {
            when(mockWsClient.url(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.withRequestTimeout(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.withFollowRedirects(true))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.get())
                .thenReturn(Future.failed(new RuntimeException("All sources failed")))

            service.fetchPreview(testUrl).map { result =>
                result.title mustBe Some("example.com")
                result.description mustBe Some("Click to visit this website")
                result.siteName mustBe Some("example.com")
            }
        }
    }
}
