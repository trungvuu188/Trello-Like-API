package services

import dto.request.column.{CreateColumnRequest, UpdateColumnRequest}
import dto.response.column.ColumnWithTasksResponse
import exception.AppException
import models.entities.Column
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import repositories.{ColumnRepository, ProjectRepository}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ColumnService @Inject()(
  columnRepository: ColumnRepository,
  projectRepository: ProjectRepository,
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def createColumn(req: CreateColumnRequest, projectId: Int, userId: Int): Future[Int] = {
    val checkAndInsert = for {
      exists <- projectRepository.isUserInActiveProject(userId, projectId)
      exitsByPosition <- columnRepository.exitsByPosition(projectId, req.position)
      _ <- if (exitsByPosition) {
        DBIO.failed(
          AppException(
            message = s"Column position ${req.position} already exists in project $projectId",
            statusCode = Status.CONFLICT
          )
        )
      } else {
        DBIO.successful(())
      }
      result <- if (exists) {
        val newColumn = Column(
          projectId = projectId,
          name = req.name,
          position = req.position
        )
        columnRepository.create(newColumn)
      } else {
        DBIO.failed(
          AppException(
            message = s"Project $projectId is not found or not active",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(checkAndInsert)
  }

  def getActiveColumnsWithTasks(projectId: Int, userId: Int): Future[Seq[ColumnWithTasksResponse]] = {
    val checkAndGet = for {
      exists <- projectRepository.isUserInActiveProject(userId, projectId)
      result <- if (exists) {
        columnRepository.findActiveColumnsWithTasks(projectId)
      } else {
        DBIO.failed(
          AppException(
            message = s"Project $projectId is not exists or not active",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(checkAndGet)
  }

  def updateColumn(req: UpdateColumnRequest, columnId: Int, projectId: Int, userId: Int): Future[Int] = {
    val checkAndUpdate = for {
      isUserInActiveProject <- projectRepository.isUserInActiveProject(projectId, userId)
      result <- if (isUserInActiveProject) {
        columnRepository.update(req, columnId)
      } else {
        DBIO.failed(
          AppException(
            message = s"Column ${columnId} is not found or not active",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(checkAndUpdate)
  }
}
