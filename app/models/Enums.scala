package models

object Enums {
  sealed trait WorkspaceStatus
  case object Active extends WorkspaceStatus
  case object Archived extends WorkspaceStatus

  sealed trait UserWorkspaceRole
  case object Admin extends UserWorkspaceRole
  case object Member extends UserWorkspaceRole

  sealed trait UserWorkspaceStatus
  case object Pending extends UserWorkspaceStatus
  case object WorkspaceActive extends UserWorkspaceStatus
  case object Inactive extends UserWorkspaceStatus

  sealed trait ProjectStatus
  case object ProjectActive extends ProjectStatus
  case object Completed extends ProjectStatus
  case object ProjectArchived extends ProjectStatus

  sealed trait TaskPriority
  case object LOW extends TaskPriority
  case object MEDIUM extends TaskPriority
  case object HIGH extends TaskPriority

  sealed trait NotificationType
  case object TaskAssigned extends NotificationType
  case object TaskCompleted extends NotificationType
  case object DeadlineApproaching extends NotificationType
  case object CommentAdded extends NotificationType
  case object TaskMoved extends NotificationType
}