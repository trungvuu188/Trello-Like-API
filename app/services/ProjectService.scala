package services

import dto.request.project.CreateProjectRequest
import dto.response.project.ProjectSummariesResponse
import exception.AppException
import models.Enums.ProjectStatus.ProjectStatus
import models.Enums.{ProjectStatus, ProjectVisibility}
import models.entities.Project
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import repositories.{ProjectRepository, WorkspaceRepository}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProjectService @Inject()(
  projectRepository: ProjectRepository,
  workspaceRepository: WorkspaceRepository,
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def createProject(req: CreateProjectRequest,
                    workspaceId: Int,
                    createdBy: Int): scala.concurrent.Future[Int] = {

    val action = for {
      // check user is part of the active workspace
      existsUserInActiveWorkspace <- workspaceRepository
        .isUserInActiveWorkspace(workspaceId, createdBy)

      projectId <- if (existsUserInActiveWorkspace) {
        val newProject = Project(
          name = req.name,
          visibility =
            ProjectVisibility.withName(req.visibility.getOrElse("workspace")),
          workspaceId = workspaceId,
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

  def getProjectsByWorkspaceAndUser(
    workspaceId: Int,
    userId: Int
  ): Future[Seq[ProjectSummariesResponse]] = {

    val action = for {
      existsUserInActiveWorkspace <- workspaceRepository
        .isUserInActiveWorkspace(workspaceId, userId)

      projects <- if (existsUserInActiveWorkspace) {
        projectRepository.findNonDeletedByWorkspace(workspaceId)
      } else {
        DBIO.failed(
          AppException(
            message = "Workspace not found",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield projects

    db.run(action)
  }

  def completeProject(projectId: Int, userId: Int): Future[Int] =
    changeStatusIfOwner(
      projectId,
      userId,
      validFrom = Set(ProjectStatus.active),
      next = ProjectStatus.completed,
      errorMsg = "Only active projects can be completed"
    )

  def deleteProject(projectId: Int, userId: Int): Future[Int] =
    changeStatusIfOwner(
      projectId,
      userId,
      validFrom = Set(ProjectStatus.completed),
      next = ProjectStatus.deleted,
      errorMsg = "Only completed projects can be deleted"
    )

  private def changeStatusIfOwner(projectId: Int,
                                  userId: Int,
                                  validFrom: Set[ProjectStatus],
                                  next: ProjectStatus,
                                  errorMsg: String): Future[Int] = {
    val action = for {
      maybeStatus <- projectRepository.findStatusIfOwner(projectId, userId)
      updatedRows <- maybeStatus match {
        case Some(s) if validFrom.contains(s) =>
          projectRepository.updateStatus(projectId, next)
        case Some(_) =>
          DBIO.failed(AppException(errorMsg, Status.BAD_REQUEST))
        case None =>
          DBIO.failed(
            AppException(
              "Project not found or you are not the owner",
              Status.NOT_FOUND
            )
          )
      }
    } yield updatedRows

    db.run(action.transactionally)
  }

  def reopenProject(projectId: Int, userId: Int): Future[Int] =
    changeStatusIfOwner(
      projectId,
      userId,
      validFrom = Set(ProjectStatus.completed, ProjectStatus.deleted),
      next = ProjectStatus.active,
      errorMsg = "Only completed or deleted projects can be reopened"
    )

}
