package models.entities

import models.Enums.{ProjectStatus, ProjectVisibility}
import models.Enums.ProjectStatus.ProjectStatus
import models.Enums.ProjectVisibility.ProjectVisibility

import java.time.{Instant, LocalDateTime}

case class Project(
                    id: Option[Int] = None,
                    name: String,
                    workspaceId: Int,
                    status: ProjectStatus = ProjectStatus.active,
                    visibility: ProjectVisibility = ProjectVisibility.Workspace,
                    createdBy: Option[Int] = None,
                    updatedBy: Option[Int] = None,
                    createdAt: Instant = Instant.now(),
                    updatedAt: Instant = Instant.now()
                  )

case class Column(
                   id: Option[Int] = None,
                   projectId: Option[Int] = None,
                   name: Option[String] = None,
                   position: Option[Int] = None,
                   createdAt: Option[LocalDateTime] = None,
                   updatedAt: Option[LocalDateTime] = None
                 )