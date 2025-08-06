package exception

import dto.response.ApiResponse
import play.api.Logger
import play.api.http.{HttpErrorHandler, Status}
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.Future

class CustomErrorHandler extends HttpErrorHandler {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val customMessage = statusCode match {
      case Status.BAD_REQUEST => "Bad request"
      case Status.UNAUTHORIZED => "Unauthorized"
      case Status.FORBIDDEN => "Forbidden"
      case Status.NOT_FOUND => "Resource not found"
      case Status.METHOD_NOT_ALLOWED => "Method not allowed"
      case _ => message
    }

    Future.successful(
      Results.Status(statusCode)(
        Json.toJson(
          ApiResponse.errorNoData(customMessage)
        )
      )
    )
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Logger("application").error("Server Error", exception)

    exception match {
      case appEx: AppException =>
        Future.successful(Results.Status(appEx.statusCode)(
          Json.toJson(
            ApiResponse.errorNoData(appEx.message)
          )
        ))

      case _ =>
        Future.successful(Results.InternalServerError(
          Json.toJson(
            ApiResponse.errorNoData(exception.getMessage)
          )
        ))
    }

  }
}
