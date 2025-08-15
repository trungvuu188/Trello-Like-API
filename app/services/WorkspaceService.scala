package services

import dto.request.workspace.{CreateWorkspaceRequest, UpdateWorkspaceRequest}
import exception.AppException
import models.entities.Workspace
import play.api.http.Status
import repositories.WorkspaceRepository

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorkspaceService @Inject()(workspaceRepository: WorkspaceRepository)(
  implicit ec: ExecutionContext
) {

  def createWorkspace(workspace: CreateWorkspaceRequest,
                      createdBy: Int): Future[Int] = {
    val now = LocalDateTime.now()
    val newWorkspace = Workspace(
      name = workspace.name,
      description = workspace.description,
      createdBy = Some(createdBy),
      createdAt = Some(now),
      updatedAt = Some(now)
    )
    workspaceRepository.createWithOwner(newWorkspace, createdBy)
  }

  def updateWorkspace(id: Int,
                      workspace: UpdateWorkspaceRequest,
                      updatedBy: Int): Future[Int] = {
    val now = LocalDateTime.now()
    val existingWorkspaceOpt = workspaceRepository.findById(id)
    existingWorkspaceOpt.flatMap {
      // Workspace exits in database, proceed with update
      case Some(existingWorkspace) =>
        // Copy existing workspace and update fields
        val updatedWorkspace = existingWorkspace.copy(
          name = workspace.name,
          description = workspace.description,
          updatedBy = Some(updatedBy),
          updatedAt = Some(now)
        )
        workspaceRepository.update(updatedWorkspace)
      // Workspace does not exist, return an error
      case None =>
        Future.failed(
          throw AppException(
            message = "Workspace not found",
            statusCode = Status.NOT_FOUND
          )
        )
    }
  }

}
