package models.entities

import models.Enums
import java.time.LocalDateTime

case class Workspace(
    id: Option[Int] = None,
    name: Option[String] = None,
    status: Enums.WorkspaceStatus = Enums.Active,
    createdBy: Option[Int] = None,
    createdAt: Option[LocalDateTime] = None,
    updatedAt: Option[LocalDateTime] = None
)

case class UserWorkspace(
    id: Option[Int] = None,
    userId: Option[Int] = None,
    workspaceId: Option[Int] = None,
    role: Option[Enums.UserWorkspaceRole] = None,
    status: Enums.UserWorkspaceStatus = Enums.WorkspaceActive,
    invitedBy: Option[Int] = None,
    joinedAt: Option[LocalDateTime] = None
)