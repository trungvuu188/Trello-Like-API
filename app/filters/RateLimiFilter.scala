package filters

import play.api.Configuration
import play.api.mvc._
import play.api.libs.json.Json
import play.api.cache.AsyncCacheApi
import dto.response.ApiResponse
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

case class RateLimitInfo(requests: Int, windowStart: Long, limit: Int, windowDuration: Long)

@Singleton
class RateLimitFilter @Inject()(
    config: Configuration,
    cache: AsyncCacheApi,
    implicit val ec: ExecutionContext
) extends EssentialFilter {

    private val enabled = config.get[Boolean]("security.rateLimit.enabled")
    private val generalLimit = config.get[Int]("security.rateLimit.general.requests")
    private val loginLimit = config.get[Int]("security.rateLimit.login.requests")
    private val registrationLimit = config.get[Int]("security.rateLimit.registration.requests")

    def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
        if (!enabled) {
            next(request)
        } else {
            val clientIp = getClientIp(request)
            val path = request.path
            val method = request.method

            // Determine rate limit based on endpoint
            val (limit, windowDuration) = getRateLimitForEndpoint(path, method)

            checkRateLimit(clientIp, path, limit, windowDuration).flatMap {
                case true =>
                    next(request)
                case false =>
                    play.api.libs.streams.Accumulator.done(
                        Results.TooManyRequests(Json.toJson(
                            ApiResponse.errorNoData("Rate limit exceeded. Please try again later.")
                        )).withHeaders(
                            "Retry-After" -> "60",
                            "X-RateLimit-Limit" -> limit.toString,
                            "X-RateLimit-Remaining" -> "0"
                        )
                    )
            }
        }
    }

    private def getClientIp(request: RequestHeader): String = {
        request.headers.get("X-Forwarded-For")
            .flatMap(_.split(",").headOption)
            .orElse(request.headers.get("X-Real-IP"))
            .getOrElse(request.remoteAddress)
    }

    private def getRateLimitForEndpoint(path: String, method: String): (Int, Long) = {
        path match {
            case p if p.contains("/auth/login") && method == "POST" =>
                (loginLimit, 1.minute.toMillis)
            case p if p.contains("/users/register") && method == "POST" =>
                (registrationLimit, 5.minutes.toMillis)
            case _ =>
                (generalLimit, 1.minute.toMillis)
        }
    }

    private def checkRateLimit(clientIp: String, endpoint: String, limit: Int, windowDuration: Long): Future[Boolean] = {
        val key = s"rate_limit:$clientIp:$endpoint"
        val now = System.currentTimeMillis()

        cache.get[RateLimitInfo](key).flatMap {
            case Some(info) =>
                if (now - info.windowStart > windowDuration) {
                    // New window
                    val newInfo = RateLimitInfo(1, now, limit, windowDuration)
                    cache.set(key, newInfo, windowDuration.millis).map(_ => true)
                } else if (info.requests >= limit) {
                    // Rate limit exceeded
                    Future.successful(false)
                } else {
                    // Increment counter
                    val updatedInfo = info.copy(requests = info.requests + 1)
                    cache.set(key, updatedInfo, (windowDuration - (now - info.windowStart)).millis)
                        .map(_ => true)
                }
            case None =>
                // First request
                val newInfo = RateLimitInfo(1, now, limit, windowDuration)
                cache.set(key, newInfo, windowDuration.millis).map(_ => true)
        }
    }
}