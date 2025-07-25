package models.tables

import models.entities.Project
import models.Enums
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import java.time.LocalDateTime

class ProjectTable(tag: Tag) extends Table[Project](tag, "projects") {
  implicit val projectStatusMapper = MappedColumnType.base[Enums.ProjectStatus, String](
    {
      case Enums.ProjectActive => "active"
      case Enums.Completed => "completed"
      case Enums.ProjectArchived => "archived"
    },
    {
      case "active" => Enums.ProjectActive
      case "completed" => Enums.Completed
      case "archived" => Enums.ProjectArchived
    }
  )

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[Option[String]]("name")
  def description = column[Option[String]]("description")
  def workspaceId = column[Option[Int]]("workspace_id")
  def status = column[Enums.ProjectStatus]("status")
  def createdBy = column[Option[Int]]("created_by")
  def updatedBy = column[Option[Int]]("updated_by")
  def createdAt = column[Option[LocalDateTime]]("created_at")
  def updatedAt = column[Option[LocalDateTime]]("updated_at")

  def * = (id.?, name, description, workspaceId, status, createdBy, updatedBy, createdAt, updatedAt) <> ((Project.apply _).tupled, Project.unapply)

  def workspaceIdIndex = index("projects_workspace_id_index", workspaceId)
  def createdByIndex = index("projects_created_by_index", createdBy)
  def statusIndex = index("projects_status_index", status)

  def workspaceFk = foreignKey("projects_workspace_id_fkey", workspaceId, TableQuery[WorkspaceTable])(_.id.?)
  def createdByFk = foreignKey("projects_created_by_fkey", createdBy, TableQuery[UserTable])(_.id.?)
  def updatedByFk = foreignKey("projects_updated_by_fkey", updatedBy, TableQuery[UserTable])(_.id.?)
}