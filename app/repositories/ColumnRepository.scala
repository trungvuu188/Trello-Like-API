package repositories

import db.MyPostgresProfile.api.{columnStatusTypeMapper, projectStatusTypeMapper}
import dto.request.column.UpdateColumnRequest
import dto.response.column.ColumnWithTasksResponse
import dto.response.task.TaskSummaryResponse
import models.Enums.ColumnStatus.ColumnStatus
import models.Enums.{ColumnStatus, ProjectStatus}
import models.entities.Column
import models.tables.TableRegistry.{columns, projects, tasks}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ColumnRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def create(column: Column): DBIO[Int] = {
    columns returning columns.map(_.id) += column
  }

  def exitsByPosition(projectId: Int, position: Int): DBIO[Boolean] = {
    columns
      .filter(c => c.projectId === projectId && c.position === position && c.status === ColumnStatus.active)
      .exists
      .result
  }

  def findActiveColumnsWithTasks(
    projectId: Int
  ): DBIO[Seq[ColumnWithTasksResponse]] = {
    columns
      .filter(
        c => c.projectId === projectId && c.status === ColumnStatus.active
      )
      .sortBy(_.position)
      .result
      .flatMap { cols =>
        tasks
          .filter(_.columnId.inSet(cols.flatMap(_.id)))
          .map(t => (t.columnId, t.id, t.name, t.position))
          .sortBy(_._4.asc.nullsLast)
          .result
          .map { taskRows =>
            val grouped = taskRows.groupBy(_._1)
            cols.map { col =>
              ColumnWithTasksResponse(
                id = col.id.get,
                name = col.name,
                position = col.position,
                tasks = grouped.getOrElse(col.id.get, Seq.empty).map {
                  case (_, id, name, pos) =>
                    TaskSummaryResponse(
                      id,
                      name,
                      pos.getOrElse(1)
                    )
                }
              )
            }
          }
      }
  }

  def update(column: UpdateColumnRequest, columnId: Int): DBIO[Int] = {
    columns
      .filter(_.id === columnId)
      .map(c => (c.name, c.updatedAt))
      .update(column.name, Instant.now())
  }

  def updateStatus(columnId: Int, status: ColumnStatus): DBIO[Int] = {
    columns.filter(_.id === columnId).map(_.status).update(status)
  }

  def findStatusIfUserInProject(columnId: Int, userId: Int): DBIO[Option[ColumnStatus]] = {
    val query = for {
      (c, p) <- columns join projects on (_.projectId === _.id)
      if c.id === columnId.bind && p.createdBy === userId.bind
    } yield c.status

    query.result.headOption
  }

  def findById(columnId: Int): DBIO[Option[Column]] = {
    columns
      .filter(c => c.id === columnId)
      .result
      .headOption
  }

}
