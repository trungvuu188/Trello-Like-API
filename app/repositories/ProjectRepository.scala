package repositories

import models.Enums
import models.Enums.UserProjectRole
import models.entities.{Project, UserProject}
import models.tables.{ProjectTable, UserProjectTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ProjectRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val projects = TableQuery[ProjectTable]
  private val userProjects = TableQuery[UserProjectTable]

  def createProjectWithOwner(project: Project,
                             ownerId: Int): DBIO[Int] = {
    for {
      // Insert project -> return với id
      projectId <- (projects returning projects.map(_.id)) += project

      // Tạo UserProject với role Owner
      userProject = UserProject(
        userId = ownerId,
        projectId = projectId,
        role = UserProjectRole.owner,
        joinedAt = Instant.now()
      )
      _ <- userProjects += userProject
    } yield projectId
  }
}
