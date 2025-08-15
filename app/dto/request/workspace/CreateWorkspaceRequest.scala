package dto.request.workspace

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes}
import validations.CustomValidators.{
  minLength,
  notEmpty,
  validateOptionalField,
  validateRequiredField
}

case class CreateWorkspaceRequest(name: String,
                                  description: Option[String] = None)

object CreateWorkspaceRequest {

  implicit val reads: Reads[CreateWorkspaceRequest] = (
    validateRequiredField[String](
      "name",
      "Workspace name is required",
      Seq(
        notEmpty("Workspace name can not be empty"),
        minLength(3, "Workspace name must be at least 3 characters")
      )
    ) and
      validateOptionalField[String](
        "description",
        Seq(notEmpty("Workspace description can not be empty")),
        _.trim
      )
  )(CreateWorkspaceRequest.apply _)

  implicit val writes: Writes[CreateWorkspaceRequest] =
    Json.writes[CreateWorkspaceRequest]
}
