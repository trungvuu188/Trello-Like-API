package models.entities

import models.Enums
import java.time.LocalDateTime

case class Project(
                    id: Option[Int] = None,
                    name: Option[String] = None,
                    description: Option[String] = None,
                    workspaceId: Option[Int] = None,
                    status: Enums.ProjectStatus = Enums.ProjectActive,
                    createdBy: Option[Int] = None,
                    updatedBy: Option[Int] = None,
                    createdAt: Option[LocalDateTime] = None,
                    updatedAt: Option[LocalDateTime] = None
                  )

case class Column(
                   id: Option[Int] = None,
                   projectId: Option[Int] = None,
                   name: Option[String] = None,
                   position: Option[Int] = None,
                   createdAt: Option[LocalDateTime] = None,
                   updatedAt: Option[LocalDateTime] = None
                 )