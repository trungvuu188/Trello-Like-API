package repositories

import models.Enums
import models.entities.{UserWorkspace, Workspace}
import models.tables.WorkspaceTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorkspaceRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    userWorkspaceRepo: UserWorkspaceRepository
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{

    import profile.api._

    // Table query object for the Workspaces table
    private val workspaces = TableQuery[WorkspaceTable]

    /** Get all workspaces */
    def getAll(): Future[Seq[Workspace]] =
        db.run(workspaces.result)

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
  /** Delete a workspace by ID */
  def delete(id: Int): Future[Int] =
    db.run(workspaces.filter(_.id === id).delete)

  /** Get a workspace by ID */
  def getWorkspaceById(id: Int): Future[Option[Workspace]] =
    db.run(workspaces.filter(_.id === id).result.headOption)

  /**
    * Updates an existing workspace in the database.
    * This method updates the workspace details based on the provided Workspace entity.
    * @param workspace The Workspace entity containing updated data.
    *                  The ID of the workspace must be set in the entity.
    * @return A Future containing the number of rows affected by the update operation.
    *         If the workspace with the specified ID does not exist, it will return 0.
    *         If the update is successful, it will return 1.
    */
  def update(workspace: Workspace): Future[Int] = {
    val query = workspaces.filter(_.id === workspace.id).update(workspace)
    db.run(query)
  }
}

