package models.entities

import models.Enums.TaskPriority
import models.Enums.TaskPriority.TaskPriority

import java.time.LocalDateTime

case class Task(
                 id: Option[Int] = None,
                 projectId: Option[Int] = None,
                 columnId: Option[Int] = None,
                 name: Option[String] = None,
                 description: Option[String] = None,
                 startDate: Option[LocalDateTime] = None,
                 endDate: Option[LocalDateTime] = None,
                 priority: TaskPriority = TaskPriority.MEDIUM,
                 position: Option[Int] = None,
                 assignedTo: Option[Int] = None,
                 createdBy: Option[Int] = None,
                 updatedBy: Option[Int] = None,
                 createdAt: Option[LocalDateTime] = None,
                 updatedAt: Option[LocalDateTime] = None
               )

case class Checklist(
                      id: Option[Int] = None,
                      taskId: Option[Int] = None,
                      name: Option[String] = None,
                      createdAt: Option[LocalDateTime] = None,
                      updatedAt: Option[LocalDateTime] = None
                    )

case class ChecklistItem(
                          id: Option[Int] = None,
                          checklistId: Option[Int] = None,
                          content: Option[String] = None,
                          isCompleted: Boolean = false,
                          createdAt: Option[LocalDateTime] = None,
                          updatedAt: Option[LocalDateTime] = None
                        )

case class TaskComment(
                        id: Option[Int] = None,
                        taskId: Option[Int] = None,
                        userId: Option[Int] = None,
                        content: Option[String] = None,
                        createdAt: Option[LocalDateTime] = None,
                        updatedAt: Option[LocalDateTime] = None
                      )

case class Tag(
                id: Option[Int] = None,
                projectId: Option[Int] = None,
                name: Option[String] = None,
                color: Option[String] = None,
                createdAt: Option[LocalDateTime] = None
              )

case class TaskTag(
                    id: Option[Int] = None,
                    taskId: Option[Int] = None,
                    tagId: Option[Int] = None
                  )
