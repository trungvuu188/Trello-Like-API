package repositories

import db.MyPostgresProfile.api.projectStatusTypeMapper
import dto.response.column.ColumnWithTasksResponse
import dto.response.task.TaskSummaryResponse
import models.Enums.ProjectStatus
import models.entities.Column
import models.tables.{ColumnTable, ProjectTable, TaskTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ColumnRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val columns = TableQuery[ColumnTable]
  private val tasks = TableQuery[TaskTable]
  private val projects = TableQuery[ProjectTable]

  def create(column: Column): DBIO[Int] = {
    columns returning columns.map(_.id) += column
  }

  def findActiveColumnsWithTasks(
    projectId: Int
  ): DBIO[Seq[ColumnWithTasksResponse]] = {
    projects
      .filter(p => p.id === projectId && p.status === ProjectStatus.active)
      .exists
      .result
      .flatMap {
        case true =>
          columns.filter(_.projectId === projectId).result.flatMap { cols =>
            tasks
              .filter(_.columnId.inSet(cols.map(_.id.get)))
              .map(t => (t.columnId, t.id, t.name, t.position))
              .result
              .map { taskRows =>
                val grouped = taskRows.groupBy(_._1)
                cols.map { col =>
                  ColumnWithTasksResponse(
                    id = col.id.get,
                    name = col.name,
                    position = col.position,
                    tasks = grouped.getOrElse(col.id, Seq.empty).map {
                      case (_, id, name, pos) =>
                        TaskSummaryResponse(
                          id,
                          name.getOrElse(""),
                          pos.getOrElse(0)
                        )
                    }
                  )
                }
              }
          }
        case false =>
          DBIO.successful(Seq.empty)
      }
  }

}
