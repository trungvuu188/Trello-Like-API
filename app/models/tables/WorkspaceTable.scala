package models.tables

import models.entities.{UserWorkspace, Workspace}
import models.Enums
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import java.time.LocalDateTime

class WorkspaceTable(tag: Tag) extends Table[Workspace](tag, "workspaces") {
  implicit val workspaceStatusMapper = MappedColumnType.base[Enums.WorkspaceStatus, String](
    {
      case Enums.Active => "active"
      case Enums.Archived => "archived"
    },
    {
      case "active" => Enums.Active
      case "archived" => Enums.Archived
    }
  )

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def name = column[Option[String]]("name")

  def status = column[Enums.WorkspaceStatus]("status")

  def createdBy = column[Option[Int]]("created_by")

  def createdAt = column[Option[LocalDateTime]]("created_at")

  def updatedAt = column[Option[LocalDateTime]]("updated_at")

  def * = (id.?, name, status, createdBy, createdAt, updatedAt) <> ((Workspace.apply _).tupled, Workspace.unapply)

}
