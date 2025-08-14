package services

import dto.response.workspace.WorkspaceResponse
import mappers.WorkspaceMapper
import repositories.WorkspaceRepository

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
}
