package dto.request.workspace

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes}
import validations.CustomValidators.{
  minLength,
  notEmpty,
  validateOptionalField,
  validateRequiredField
}

case class UpdateWorkspaceRequest(name: String,
                                  description: Option[String] = None)

object UpdateWorkspaceRequest {

  implicit val reads: Reads[UpdateWorkspaceRequest] = (
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
  )(UpdateWorkspaceRequest.apply _)

  implicit val writes: Writes[UpdateWorkspaceRequest] =
    Json.writes[UpdateWorkspaceRequest]
}
