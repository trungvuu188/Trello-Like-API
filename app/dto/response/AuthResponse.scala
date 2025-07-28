package dto.response

import play.api.libs.json.{Format, Json}

case class AuthResponse (
    success: Boolean,
    message: String
)

object AuthResponse {
    implicit val format: Format[AuthResponse] = Json.format[AuthResponse]
}
