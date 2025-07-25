package controllers

import dto.response.ApiResponse

import javax.inject._
import play.api.mvc._
import play.api.libs.json._


import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiBaseController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // Helper method to return JSON response with same format
  def apiResult[T](data: T, message: String = "Success", status: Status = Ok)
                  (implicit writes: Writes[T]): Result = {
    val response = ApiResponse(message, data)
    status(Json.toJson(response))
  }

  // Error responses
  def apiError[T](data: T, message: String, status: Status = BadRequest)
                 (implicit writes: Writes[T]): Result = {
    val response = ApiResponse(message, data)
    status(Json.toJson(response))
  }
}