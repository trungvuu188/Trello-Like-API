package controllers

import dto.response.ApiResponse
import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiBaseController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  // Helper method to return JSON response with data
  def apiResult[T](data: T, message: String = "Success", status: Status = Ok)
                  (implicit writes: Writes[T]): Result = {
    val response = ApiResponse.success(message, data) // Use ApiResponse.success with data
    status(Json.toJson(response))
  }

  // Helper method to return JSON response without data
  def apiResultNoData(message: String = "Success", status: Status = Ok): Result = {
    val response = ApiResponse.successNoData(message) // Use ApiResponse.successNoData
    status(Json.toJson(response))
  }

  // Success responses with data
  def apiSuccess[T](data: T, message: String = "Success")
                   (implicit writes: Writes[T]): Result = {
    val response = ApiResponse.success(message, data)
    Ok(Json.toJson(response))
  }

  // Success responses without data
  def apiSuccessNoData(message: String = "Success"): Result = {
    val response = ApiResponse.successNoData(message)
    Ok(Json.toJson(response))
  }

  // Error responses with data
  def apiError[T](data: T, message: String = "Error", status: Status = BadRequest)
                 (implicit writes: Writes[T]): Result = {
    val response = ApiResponse.withData(message, data) // Use ApiResponse.withData
    status(Json.toJson(response))
  }

  // Error responses without data (most common for errors)
  def apiErrorNoData(message: String = "Error", status: Status = BadRequest): Result = {
    val response = ApiResponse.errorNoData(message) // Use ApiResponse.errorNoData
    status(Json.toJson(response))
  }

  // Alternative: More explicit error method without data
  def apiErrorMessage(message: String = "Error", status: Status = BadRequest): Result = {
    val response = ApiResponse.withoutData[String](message) // Use ApiResponse.withoutData
    status(Json.toJson(response))
  }

  // Async versions
  def apiResultAsync[T](data: T, message: String = "Success", status: Status = Ok)
                       (implicit writes: Writes[T]): Future[Result] = {
    Future.successful(apiResult(data, message, status))
  }

  def apiSuccessAsync[T](data: T, message: String = "Success")
                        (implicit writes: Writes[T]): Future[Result] = {
    Future.successful(apiSuccess(data, message))
  }

  def apiSuccessNoDataAsync(message: String = "Success"): Future[Result] = {
    Future.successful(apiSuccessNoData(message))
  }

  def apiErrorAsync[T](data: T, message: String = "Error", status: Status = BadRequest)
                      (implicit writes: Writes[T]): Future[Result] = {
    Future.successful(apiError(data, message, status))
  }

  def apiErrorNoDataAsync(message: String = "Error", status: Status = BadRequest): Future[Result] = {
    Future.successful(apiErrorNoData(message, status))
  }
}