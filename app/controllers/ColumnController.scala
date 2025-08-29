package controllers

import dto.request.column.{CreateColumnRequest, UpdateColumnRequest}
import dto.response.ApiResponse
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{
  Action,
  AnyContent,
  MessagesAbstractController,
  MessagesControllerComponents
}
import services.ColumnService
import utils.WritesExtras.unitWrites
import validations.ValidationHandler

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ColumnController @Inject()(
  cc: MessagesControllerComponents,
  columnService: ColumnService,
  authenticatedActionWithUser: AuthenticatedActionWithUser
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ValidationHandler {

  /** POST /projects/:projectId/columns */
  def create(projectId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val createdBy = request.userToken.userId
      handleJsonValidation[CreateColumnRequest](request.body) {
        createColumnDto =>
          columnService
            .createColumn(createColumnDto, projectId, createdBy)
            .map { columnId =>
              Created(
                Json.toJson(
                  ApiResponse[Unit](
                    s"Column created successfully with ID: $columnId"
                  )
                )
              )
            }
      }
    }

  /** GET /projects/:projectId/columns */
  def getAll(projectId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      columnService.getActiveColumnsWithTasks(projectId, userId).map {
        columns =>
          val apiResponse =
            ApiResponse(
              message = "Columns retrieved",
              data = Some(Json.toJson(columns))
            )
          Ok(Json.toJson(apiResponse))
      }
    }

  /** PATCH /projects/:projectId/columns/:columnId */
  def update(projectId: Int, columnId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val updatedBy = request.userToken.userId
      handleJsonValidation[UpdateColumnRequest](request.body) {
        updateColumnDto =>
          columnService
            .updateColumn(updateColumnDto, columnId, projectId, updatedBy)
            .map { _ =>
              Ok(Json.toJson(ApiResponse[Unit]("Column updated successfully")))
            }
      }
    }
}
