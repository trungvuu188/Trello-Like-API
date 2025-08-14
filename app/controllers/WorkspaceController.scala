package controllers

import dto.response.ApiResponse
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.WorkspaceService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class WorkspaceController @Inject() (
    cc: ControllerComponents,
    workspaceService: WorkspaceService
) (implicit ec: ExecutionContext) extends AbstractController(cc) {

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
}
