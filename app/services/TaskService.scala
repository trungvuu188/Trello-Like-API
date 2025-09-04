package services

import dto.request.task.UpdateTaskRequest
import dto.response.task.TaskDetailResponse
import exception.AppException
import mappers.TaskMapper
import models.Enums.{ColumnStatus, TaskStatus}
import models.Enums.TaskStatus.TaskStatus
import models.entities.{Column, Task}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import repositories.{ColumnRepository, ProjectRepository, TaskRepository}
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskService @Inject()(taskRepository: TaskRepository,
                            columnRepository: ColumnRepository,
                            projectRepository: ProjectRepository,
                            protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]  {

  import profile.api._

  def createNewTask(taskName: String, columnId: Int, createdBy: Int): Future[Int] = {
    val action = for {
      _ <- verifyColumnAndUserProjectAccess(columnId, createdBy)

      existByName <- taskRepository.findByNameAndActiveTrueInColumn(taskName, columnId)
      _ <- existByName match {
        case Some(_) =>
          DBIO.failed(AppException(
            message = s"Task name already exists in the column",
            statusCode = Status.CONFLICT)
          )
        case None => DBIO.successful(())
      }

      maxPosition <- taskRepository.getMaxPosition(columnId)

      taskId <- {
        val newTask = Task(
          name = taskName,
          columnId = columnId,
          createdBy = Some(createdBy),
          updatedBy = Some(createdBy),
          position = Some(maxPosition + 1)
        )
        taskRepository.create(newTask)
      }
    } yield taskId

    db.run(action.transactionally)
  }

  def updateTask(taskId: Int, req: UpdateTaskRequest, updatedBy: Int): Future[Int] = {
    val action = for {
      taskOpt <- getTaskById(taskId, updatedBy)
      task <- taskOpt match {
        case Some(t) if t.status == models.Enums.TaskStatus.active => DBIO.successful(t)
        case _ => DBIO.failed(AppException(
          message = s"Task with ID $taskId does not exist or is not active",
          statusCode = Status.NOT_FOUND)
        )
      }

      existByName <- taskRepository.findByNameAndActiveTrueInColumn(req.name, task.columnId)
      _ <- existByName match {
        case Some(t) if t.id.get != taskId =>
          DBIO.failed(AppException(
            message = s"Task name already exists in the column",
            statusCode = Status.CONFLICT)
          )
        case _ => DBIO.successful(())
      }

      updatedTask = task.copy(
        name = req.name,
        description = req.description,
        startDate = req.startDate,
        endDate = req.endDate,
        priority = req.priority,
        updatedBy = Some(updatedBy),
        updatedAt = Instant.now(),
        isCompleted = req.isCompleted.getOrElse(false)
      )

      rowsAffected <- taskRepository.update(updatedTask)

    } yield rowsAffected

    db.run(action.transactionally)
  }

  def getTaskDetailById(taskId: Int, userId: Int): Future[Option[TaskDetailResponse]] = {
    val action = for {
      task <- getTaskById(taskId, userId)
    } yield task

    db.run(action.transactionally).map(_.map(TaskMapper.toDetailResponse))
  }

  def archiveTask(taskId: Int, userId: Int): Future[Int] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.active),
      next = TaskStatus.archived,
      errorMsg = "Only active tasks can be archived"
    )
  }

  def restoreTask(taskId: Int, userId: Int): Future[Int] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.archived),
      next = TaskStatus.active,
      errorMsg = "Only archived tasks can be restored"
    )
  }

  def deleteTask(taskId: Int, userId: Int): Future[Int] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.archived),
      next = TaskStatus.deleted,
      errorMsg = "Only archived tasks can be deleted"
    )
  }

  private def changeStatus(taskId: Int,
                           userId: Int,
                           validFrom: Set[TaskStatus],
                           next: TaskStatus,
                           errorMsg: String): Future[Int] = {
    val action = for {
      task <- getTaskById(taskId, userId)
      maybeStatus = task.map(_.status)
      updatedRows <- maybeStatus match {
        case Some(s) if validFrom.contains(s) =>
          taskRepository.update(task.get.copy(
            status = next,
            updatedBy = Some(userId),
            updatedAt = Instant.now()
          ))
        case Some(_) =>
          DBIO.failed(AppException(errorMsg, Status.BAD_REQUEST))
        case None =>
          DBIO.failed(AppException(
            message = s"Task with ID $taskId does not exist",
            statusCode = Status.NOT_FOUND)
          )
      }
    } yield updatedRows

    db.run(action)
  }

  private def getTaskById(taskId: Int, userId: Int): DBIO[Option[Task]] = {
    for {
      taskOpt <- taskRepository.findById(taskId)
      task <- taskOpt match {
        case Some(t) => DBIO.successful(Some(t))
        case _ => DBIO.failed(AppException(
          message = s"Task with ID $taskId does not exist",
          statusCode = Status.NOT_FOUND)
        )
      }
      _ <- task match {
        case Some(t) => verifyColumnAndUserProjectAccess(t.columnId, userId).map(_ => ())
      }
    } yield task
  }

  private def verifyColumnAndUserProjectAccess(columnId: Int, userId: Int): DBIO[Column] = {
    for {
      columnOpt <- columnRepository.findById(columnId)
      column <- columnOpt match {
        case Some(col) if col.status == ColumnStatus.active => DBIO.successful(col)
        case _ => DBIO.failed(AppException(
          message = s"Column with ID $columnId does not exist or is not active",
          statusCode = Status.NOT_FOUND)
        )
      }

      // Check if user is part of the active project
      existsUserInActiveProject <- projectRepository.isUserInActiveProject(userId, column.projectId)
      _ <- if (existsUserInActiveProject) {
        DBIO.successful(())
      } else {
        DBIO.failed(AppException(
          message = s"Project is inactive or user is not a member of the project",
          statusCode = Status.FORBIDDEN)
        )
      }
    } yield column
  }



}
