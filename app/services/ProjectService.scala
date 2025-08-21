package services

import dto.request.project.CreateProjectRequest
import exception.AppException
import models.Enums.ProjectVisibility
import models.entities.Project
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import repositories.{ProjectRepository, WorkspaceRepository}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ProjectService @Inject()(
  projectRepository: ProjectRepository,
  workspaceRepository: WorkspaceRepository,
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def createProject(req: CreateProjectRequest,
                    createdBy: Int): scala.concurrent.Future[Int] = {

    val action = for {
      exists <- workspaceRepository.existById(req.workspaceId)

      projectId <- if (exists) {
        val newProject = Project(
          name = req.name,
          visibility =
            ProjectVisibility.withName(req.visibility.getOrElse("workspace")),
          workspaceId = req.workspaceId,
          createdBy = Some(createdBy),
          updatedBy = Some(createdBy)
        )
        projectRepository.createProjectWithOwner(newProject, createdBy)
      } else {
        DBIO.failed(
          AppException(
            message = "Workspace not found",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield projectId

    db.run(action.transactionally)
  }
}
