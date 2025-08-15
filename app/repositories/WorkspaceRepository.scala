package repositories

import models.Enums
import models.entities.{UserWorkspace, Workspace}
import models.tables.TableRegistry
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorkspaceRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider,
  userWorkspaceRepo: UserWorkspaceRepository
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val workspaces = TableRegistry.workspaces

  /**
    * Creates a new workspace and associates it with the specified user as an owner.
    * This method performs the creation of both the workspace and the corresponding UserWorkspace entry
    * in a single transaction.
    *
    * @param workspace The Workspace entity to create.
    * @param userId The ID of the user who will own the workspace.
    * @return A Future containing the ID of the created workspace.
    */
  def createWithOwner(workspace: Workspace, userId: Int): Future[Int] = {
    val action = for {
      // Create workspace
      wsId <- (workspaces returning workspaces.map(_.id)) += workspace

      // Create corresponding UserWorkspace entry
      _ <- userWorkspaceRepo.insertAction(
        UserWorkspace(
          workspaceId = Some(wsId),
          userId = Some(userId),
          role = Some(Enums.UserWorkspaceRole.admin)
        )
      )
    } yield wsId

    db.run(action.transactionally)
  }
}
