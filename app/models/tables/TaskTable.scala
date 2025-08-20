package models.tables

import models.entities.Task
import models.Enums.TaskPriority.TaskPriority
import db.MyPostgresProfile.api._
import slick.lifted.Tag

import java.time.LocalDateTime

class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def projectId = column[Option[Int]]("project_id")
  def columnId = column[Option[Int]]("column_id")
  def name = column[Option[String]]("name")
  def description = column[Option[String]]("description")
  def startDate = column[Option[LocalDateTime]]("start_date")
  def endDate = column[Option[LocalDateTime]]("end_date")
  def priority = column[TaskPriority]("priority")
  def position = column[Option[Int]]("position")
  def assignedTo = column[Option[Int]]("assigned_to")
  def createdBy = column[Option[Int]]("created_by")
  def updatedBy = column[Option[Int]]("updated_by")
  def createdAt = column[Option[LocalDateTime]]("created_at")
  def updatedAt = column[Option[LocalDateTime]]("updated_at")

  def * =
    (id.?, projectId, columnId, name, description, startDate, endDate, priority, position, assignedTo,
      createdBy, updatedBy, createdAt, updatedAt) <>
    ((Task.apply _).tupled, Task.unapply)

  def columnPositionIndex = index("tasks_column_id_position_index", (columnId, position))
  def projectIdIndex = index("tasks_project_id_index", projectId)
  def assignedToIndex = index("tasks_assigned_to_index", assignedTo)
  def createdByIndex = index("tasks_created_by_index", createdBy)
  def startDateIndex = index("tasks_start_date_index", startDate)
  def endDateIndex = index("tasks_end_date_index", endDate)

  def projectFk = foreignKey("tasks_project_id_fkey", projectId, TableQuery[ProjectTable])(_.id.?)
  def columnFk = foreignKey("tasks_column_id_fkey", columnId, TableQuery[ColumnTable])(_.id.?)
  def assignedToFk = foreignKey("tasks_assigned_to_fkey", assignedTo, TableQuery[UserTable])(_.id.?)
  def createdByFk = foreignKey("tasks_created_by_fkey", createdBy, TableQuery[UserTable])(_.id.?)
  def updatedByFk = foreignKey("tasks_updated_by_fkey", updatedBy, TableQuery[UserTable])(_.id.?)
}
