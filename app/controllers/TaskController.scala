package controllers

import dto.request.task.{CreateTaskRequest, UpdateTaskRequest}
import dto.response.ApiResponse
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import services.TaskService
import validations.ValidationHandler
import utils.WritesExtras.unitWrites

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TaskController @Inject()(
                                cc: MessagesControllerComponents,
                                taskService: TaskService,
                                authenticatedActionWithUser: AuthenticatedActionWithUser
                              )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc)
    with ValidationHandler {

  def create(columnId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val createdBy = request.userToken.userId
      handleJsonValidation[CreateTaskRequest](request.body) {
        createColumnDto =>
          taskService
            .createNewTask(createColumnDto.name, columnId, createdBy)
            .map { taskId =>
              Created(
                Json.toJson(
                  ApiResponse[Unit](
                    s"Task created successfully with ID: $taskId"
                  )
                )
              )
            }
      }
    }

  def update(taskId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val updatedBy = request.userToken.userId
      handleJsonValidation[UpdateTaskRequest](request.body) {
        updateTaskDto =>
          taskService
            .updateTask(taskId, updateTaskDto, updatedBy)
            .map { _ =>
              Ok(
                Json.toJson(
                  ApiResponse[Unit](
                    s"Task updated successfully"
                  )
                )
              )
            }
      }
    }

  def getById(taskId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      taskService
        .getTaskDetailById(taskId, userId)
        .map { taskDetail =>
          Ok(
            Json.toJson(
              ApiResponse.success("Task retrieved successfully", taskDetail)
            )
          )
        }
    }

  def archive(taskId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val archivedBy = request.userToken.userId
      taskService
        .archiveTask(taskId, archivedBy)
        .map { _ =>
          Ok(
            Json.toJson(
              ApiResponse[Unit](
                s"Task archived successfully"
              )
            )
          )
        }
    }

  def restore(taskId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val restoredBy = request.userToken.userId
      taskService
        .restoreTask(taskId, restoredBy)
        .map { _ =>
          Ok(
            Json.toJson(
              ApiResponse[Unit](
                s"Task restored successfully"
              )
            )
          )
        }
    }

  def delete(taskId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val deletedBy = request.userToken.userId
      taskService
        .deleteTask(taskId, deletedBy)
        .map { _ =>
          Ok(
            Json.toJson(
              ApiResponse[Unit](
                s"Task deleted successfully"
              )
            )
          )
        }
    }

}
