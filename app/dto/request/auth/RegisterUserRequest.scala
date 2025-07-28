package dto.request.auth

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{email, maxLength, minLength}

case class RegisterUserRequest(
                                name: String,
                                email: String,
                                password: String
                              )

object RegisterUserRequest {
  implicit val reads: Reads[RegisterUserRequest] = (
    (JsPath \ "name").read[String](minLength[String](1) keepAnd maxLength[String](100)).map(_.trim) and
      (JsPath \ "email").read[String](email).map(_.trim.toLowerCase) and
      (JsPath \ "password").read[String](minLength[String](6) keepAnd maxLength[String](50))
    )(RegisterUserRequest.apply _)

  implicit val writes: Writes[RegisterUserRequest] = Json.writes[RegisterUserRequest]
}

