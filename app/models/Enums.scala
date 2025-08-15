package models

object Enums {

  object UserWorkspaceRole extends Enumeration {
    type UserWorkspaceRole = Value
    val admin, member = Value
  }

  object UserWorkspaceStatus extends Enumeration {
    type UserWorkspaceStatus = Value
    val pending, active, inactive = Value
  }

  object ProjectStatus extends Enumeration {
    type ProjectStatus = Value
    val active, completed, archived = Value
  }

  object TaskPriority extends Enumeration {
    type TaskPriority = Value
    val LOW, MEDIUM, HIGH = Value
  }

  object NotificationType extends Enumeration {
    type NotificationType = Value
    val task_assigned, task_completed, deadline_approaching, comment_added,
    task_moved = Value
  }

  object WorkspaceStatus extends Enumeration {
    type WorkspaceStatus = Value
    val active, archived = Value
  }

}
