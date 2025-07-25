package models.tables

import models.entities.UserWorkspace
import models.Enums
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import java.time.LocalDateTime

class UserWorkspaceTable(tag: Tag) extends Table[UserWorkspace](tag, "user_workspaces") {
  implicit val userWorkspaceRoleMapper = MappedColumnType.base[Enums.UserWorkspaceRole, String](
    {
      case Enums.Admin => "admin"
      case Enums.Member => "member"
    },
    {
      case "admin" => Enums.Admin
      case "member" => Enums.Member
    }
  )

  implicit val userWorkspaceStatusMapper = MappedColumnType.base[Enums.UserWorkspaceStatus, String](
    {
      case Enums.Pending => "pending"
      case Enums.WorkspaceActive => "active"
      case Enums.Inactive => "inactive"
    },
    {
      case "pending" => Enums.Pending
      case "active" => Enums.WorkspaceActive
      case "inactive" => Enums.Inactive
    }
  )

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Option[Int]]("user_id")
  def workspaceId = column[Option[Int]]("workspace_id")
  def role = column[Option[Enums.UserWorkspaceRole]]("role")
  def status = column[Enums.UserWorkspaceStatus]("status")
  def invitedBy = column[Option[Int]]("invited_by")
  def joinedAt = column[Option[LocalDateTime]]("joined_at")

  def * = (id.?, userId, workspaceId, role, status, invitedBy, joinedAt) <> ((UserWorkspace.apply _).tupled, UserWorkspace.unapply)

  def userWorkspaceIndex = index("user_workspaces_user_id_workspace_id_unique", (userId, workspaceId), unique = true)
  def userIdIndex = index("user_workspaces_user_id_index", userId)
  def workspaceIdIndex = index("user_workspaces_workspace_id_index", workspaceId)

  def userFk = foreignKey("user_workspaces_user_id_fkey", userId, TableQuery[UserTable])(_.id.?)
  def workspaceFk = foreignKey("user_workspaces_workspace_id_fkey", workspaceId, TableQuery[WorkspaceTable])(_.id.?)
  def invitedByFk = foreignKey("user_workspaces_invited_by_fkey", invitedBy, TableQuery[UserTable])(_.id.?)
}
