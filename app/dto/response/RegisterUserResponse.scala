package dto.response

import play.api.libs.json.{Json, OWrites}

case class RegisterUserResponse(
                                 id: Int,
                                 name: String,
                                 email: String,
                               )

object RegisterUserResponse {
  implicit val writes: OWrites[RegisterUserResponse] = Json.writes[RegisterUserResponse]
}

