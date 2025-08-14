package controllers

import dto.request.workspace.{CreateWorkspaceRequest, UpdateWorkspaceRequest}
import dto.response.ApiResponse
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
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
class WorkspaceController @Inject() (
    cc: ControllerComponents,
    workspaceService: WorkspaceService
) (implicit ec: ExecutionContext) extends AbstractController(cc) {

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
    /** GET /workspaces */
    def getAllWorkspaces: Action[AnyContent] = Action.async {
        workspaceService.getAllWorkspaces.map { workspaces =>
            val apiResponse = ApiResponse.success("Workspaces retrieved", workspaces)
            Ok(Json.toJson(apiResponse))
        }
    }

    /** GET /workspaces/:id */
    def getWorkspaceById(id: Int): Action[AnyContent] = Action.async {
        workspaceService.getWorkspaceById(id).map {
            case Some(workspace) =>
                val apiResponse = ApiResponse.success("Workspace retrieved ok", workspace)
                Ok(Json.toJson(apiResponse))
            case None            => NotFound(Json.obj("error" -> s"Workspace $id not found"))
        }
    }

    /** DELETE /workspaces/:id */
    def deleteWorkspace(id: Int): Action[AnyContent] = Action.async {
        workspaceService.deleteWorkspace(id).map {
            case true =>
                Ok(Json.toJson(ApiResponse.successNoData("Workspace deleted successfully")))
            case false =>
                NotFound(Json.toJson(ApiResponse.errorNoData("Workspace not found or could not be deleted")))
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
