package dto.response

import play.api.libs.json._

case class ApiResponse[T](
                           message: String,
                           data: T
                         )

object ApiResponse {
  implicit def writes[T](implicit tWrites: Writes[T]): Writes[ApiResponse[T]] = (response: ApiResponse[T]) => Json.obj(
    "message" -> response.message,
    "data" -> Json.toJson(response.data)
  )

  // Helper methods to create quick response
  def success[T](data: T, message: String = "Success"): ApiResponse[T] =
    ApiResponse(message, data)

  def error[T](data: T, message: String = "Error"): ApiResponse[T] =
    ApiResponse(message, data)
}
