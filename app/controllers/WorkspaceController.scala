package controllers

import dto.request.workspace.{CreateWorkspaceRequest, UpdateWorkspaceRequest}
import dto.response.ApiResponse
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import services.WorkspaceService
import utils.WritesExtras.unitWrites
import validations.ValidationHandler

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class WorkspaceController @Inject()(
  cc: ControllerComponents,
  workspaceService: WorkspaceService,
  authenticatedActionWithUser: AuthenticatedActionWithUser
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ValidationHandler {

  def create(): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      val createdBy = request.userToken.userId
      handleJsonValidation[CreateWorkspaceRequest](request.body) {
        createWorkspaceDto =>
          workspaceService.createWorkspace(createWorkspaceDto, createdBy).map {
            workspaceId =>
              Created(
                Json.toJson(ApiResponse[Unit]("Workspace created successfully"))
              )
          }
      }
    }
  def update(id: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      val updatedBy = request.userToken.userId
      handleJsonValidation[UpdateWorkspaceRequest](request.body) {
        updateWorkspaceDto =>
          workspaceService
            .updateWorkspace(id, updateWorkspaceDto, updatedBy)
            .map { _ =>
              Ok(
                Json.toJson(ApiResponse[Unit]("Workspace updated successfully"))
              )
            }
      }
    }
}
