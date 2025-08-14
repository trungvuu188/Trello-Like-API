package repositories

import models.entities.Workspace
import models.tables.WorkspaceTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorkspaceRepository @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{

    import profile.api._

    // Table query object for the Workspaces table
    private val workspaces = TableQuery[WorkspaceTable]

    /** Get all workspaces */
    def getAll(): Future[Seq[Workspace]] =
        db.run(workspaces.result)

    /** Get a workspace by ID */
    def getWorkspaceById(id: Int): Future[Option[Workspace]] =
        db.run(workspaces.filter(_.id === id).result.headOption)

    /** Delete a workspace by ID */
    def delete(id: Int): Future[Int] =
        db.run(workspaces.filter(_.id === id).delete)
}

