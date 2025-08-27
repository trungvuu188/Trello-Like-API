package models.tables

import models.entities.Column
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import java.time.LocalDateTime

class ColumnTable(tag: Tag) extends Table[Column](tag, "columns") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def projectId = column[Option[Int]]("project_id")
  def name = column[Option[String]]("name")
  def position = column[Option[Int]]("position")
  def createdAt = column[Option[LocalDateTime]]("created_at")
  def updatedAt = column[Option[LocalDateTime]]("updated_at")

  def * = (id.?, projectId, name, position, createdAt, updatedAt) <> ((Column.apply _).tupled, Column.unapply)

  def projectPositionIndex = index("columns_project_id_position_index", (projectId, position))
  def projectIdIndex = index("columns_project_id_index", projectId)

  def projectFk = foreignKey("columns_project_id_fkey", projectId, TableQuery[ProjectTable])(_.id.?)
}
