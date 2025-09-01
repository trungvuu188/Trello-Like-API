package dto.request.task

import play.api.i18n.Messages
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{notEmpty, validateRequiredField}

case class CreateTaskRequest(name: String)

object CreateTaskRequest {

  implicit def reads(implicit messages: Messages): Reads[CreateTaskRequest] =
    validateRequiredField[String](
      "name",
      ErrorMessages.required("Task name"),
      Seq(
        notEmpty(ErrorMessages.empty("Task name"))
      ),
      _.trim
    ).map(CreateTaskRequest.apply)

  implicit val writes: Writes[CreateTaskRequest] =
    Json.writes[CreateTaskRequest]
}