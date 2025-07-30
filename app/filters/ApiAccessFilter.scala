package filters

import play.api.Configuration
import play.api.mvc._
import play.api.libs.json.Json
import services.{CookieService, JwtService, RoleService}
import dto.response.ApiResponse
import play.api.libs.streams.Accumulator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ApiAccessFilter @Inject()(
    config: Configuration,
    jwtService: JwtService,
    cookieService: CookieService,
    roleService: RoleService,
    implicit val ec: ExecutionContext
) extends EssentialFilter {

    private val publicEndpoints = config.get[Seq[String]]("api.publicEndpoints")
    private val adminEndpoints = config.get[Seq[String]]("api.adminEndpoints")
    private val userEndpoints = config.get[Seq[String]]("api.userEndpoints")

    def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>
        val path = request.path

        // Skip non-API requests
        if (!path.startsWith("/api/")) {
            next(request)
        } else if (isPublicEndpoint(path)) {
            // Public endpoints don't need authentication
            next(request)
        } else {
            // Extract and validate JWT token
            cookieService.getTokenFromRequest(request) match {
                case Some(token) =>
                    jwtService.validateToken(token) match {
                        case Success(userToken) =>
                            // Check authorization based on endpoint type
                            if (isAdminEndpoint(path)) {
                                val userId = userToken.userId.toInt
                                val adminCheckFuture = roleService.getUserRole(userId)


                            } else {
                                // User is authenticated and authorized
                                next(request)
                            }
                        case Failure(_) =>
                            play.api.libs.streams.Accumulator.done(
                                Results.Unauthorized(Json.toJson(
                                    ApiResponse.errorNoData("Invalid or expired token")
                                )).withCookies(cookieService.createExpiredAuthCookie())
                            )
                    }
                case None =>
                    play.api.libs.streams.Accumulator.done(
                        Results.Unauthorized(Json.toJson(
                            ApiResponse.errorNoData("Authentication required")
                        ))
                    )
            }
        }
    }

    private def isPublicEndpoint(path: String): Boolean = {
        publicEndpoints.exists(endpoint => matchesPattern(path, endpoint))
    }

    private def isAdminEndpoint(path: String): Boolean = {
        adminEndpoints.exists(endpoint => matchesPattern(path, endpoint))
    }

    private def matchesPattern(path: String, pattern: String): Boolean = {
        if (pattern.endsWith("/*")) {
            val prefix = pattern.dropRight(2)
            path.startsWith(prefix)
        } else {
            path == pattern
        }
    }
}