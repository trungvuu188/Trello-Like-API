package services

import dto.request.workspace.{CreateWorkspaceRequest, UpdateWorkspaceRequest}
import dto.response.workspace.WorkspaceResponse
import exception.AppException
import mappers.WorkspaceMapper
import models.entities.Workspace
import play.api.http.Status
import repositories.WorkspaceRepository

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorkspaceService @Inject() (
    workspaceRepo: WorkspaceRepository
) (implicit ec: ExecutionContext) {

    /** Get all workspaces */
    def getAllWorkspaces: Future[Seq[WorkspaceResponse]] =
        workspaceRepo.getAll().map(WorkspaceMapper.toResponses)

    /** Get workspace by ID */
    def getWorkspaceById(id: Int): Future[Option[WorkspaceResponse]] =
        workspaceRepo.getWorkspaceById(id).map(_.map(WorkspaceMapper.toResponse))

    /** Delete a workspace by ID */
    def deleteWorkspace(id: Int): Future[Boolean] =
        workspaceRepo.delete(id).map(_ > 0)

    def createWorkspace(workspace: CreateWorkspaceRequest, createdBy: Int): Future[Int] = {
        val now = LocalDateTime.now()
        val newWorkspace = Workspace(
            name = workspace.name,
            description = workspace.description,
            createdBy = Some(createdBy),
            createdAt = Some(now),
            updatedAt = Some(now)
        )
        workspaceRepo.createWithOwner(newWorkspace, createdBy)
    }

  def updateWorkspace(id: Int,
                      workspace: UpdateWorkspaceRequest,
                      updatedBy: Int): Future[Int] = {
    val now = LocalDateTime.now()
    val existingWorkspaceOpt = workspaceRepo.getWorkspaceById(id)
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
        workspaceRepo.update(updatedWorkspace)
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
