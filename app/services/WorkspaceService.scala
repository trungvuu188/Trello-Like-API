package services

import dto.request.workspace.CreateWorkspaceRequest
import models.entities.Workspace
import repositories.WorkspaceRepository

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorkspaceService @Inject()(workspaceRepository: WorkspaceRepository)(
  implicit ec: ExecutionContext
) {

  def createWorkspace(workspace: CreateWorkspaceRequest, createdBy: Int): Future[Int] = {
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

}
