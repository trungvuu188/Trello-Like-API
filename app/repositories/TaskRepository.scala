package repositories

import db.MyPostgresProfile.api.taskStatusTypeMapper
import models.Enums.TaskStatus
import models.entities.Task
import models.tables.TaskTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TaskRepository@Inject()(
                               protected val dbConfigProvider: DatabaseConfigProvider
                             )(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val tasks = TableQuery[TaskTable]

  def create(task: Task): DBIO[Int] =
    tasks returning tasks.map(_.id) += task

  def getMaxPosition(columnId: Int): DBIO[Int] = {
    tasks.filter(t => t.columnId === columnId && t.status === TaskStatus.active)
      .map(_.position)
      .max
      .result
      .map(_.getOrElse(0))
  }

  def findByNameAndActiveTrueInColumn(name: String, columnId: Int): DBIO[Option[Task]] = {
    tasks
      .filter(t => t.name === name && t.columnId === columnId && t.status === TaskStatus.active)
      .result.headOption
  }

  def findById(id: Int): DBIO[Option[Task]] = {
    tasks.filter(_.id === id).result.headOption
  }

  def update(task: Task): DBIO[Int] = {
    tasks.filter(_.id === task.id).update(task)
  }


}
