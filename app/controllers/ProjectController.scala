package controllers

import dto.request.project.CreateProjectRequest
import dto.response.ApiResponse
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import services.ProjectService
import utils.WritesExtras.unitWrites
import validations.ValidationHandler

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ProjectController @Inject()(
  cc: MessagesControllerComponents,
  projectService: ProjectService,
  authenticatedActionWithUser: AuthenticatedActionWithUser
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ValidationHandler {

  /** POST /projects */
  def create() = authenticatedActionWithUser.async(parse.json) { request =>
    implicit val messages: Messages = request.messages
    val createdBy = request.userToken.userId
    handleJsonValidation[CreateProjectRequest](request.body) {
      createProjectDto =>
        projectService.createProject(createProjectDto, createdBy).map {
          projectId =>
            Created(
              Json.toJson(
                ApiResponse[Unit](
                  s"Project created successfully with ID: $projectId"
                )
              )
            )
        }
    }
  }
}
