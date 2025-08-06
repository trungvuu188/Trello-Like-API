package repositories

import com.typesafe.config.ConfigFactory
import models.entities.User
import models.tables.TableRegistry
import org.scalatest.BeforeAndAfterEach
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, Configuration}
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.JdbcProfile

import java.time.LocalDateTime
import scala.concurrent.{Await, ExecutionContext}

class UserRepositorySpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach {

  // configuration for the test application
  override def fakeApplication(): Application = {
    val config = ConfigFactory.load("application.test.conf")
    new GuiceApplicationBuilder()
      .configure(Configuration(config))
      .build()
  }

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  val userRepository = new UserRepository(dbConfigProvider)(ExecutionContext.global)

  override def beforeEach(): Unit = {
    val users = TableRegistry.users
    val roles = TableRegistry.roles

    val setup = DBIO.seq(
      users.schema.dropIfExists,
      roles.schema.dropIfExists,
      roles.schema.create,
      users.schema.create,
      roles += models.entities.Role(Some(1), "user"),
      users ++= Seq(
        User(
          id = Some(1),
          name = "test user 1",
          email = "user1@test.com",
          password = "hashed_password",
          roleId = Some(1),
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now(),
        )
      )
    )
    Await.result(db.run(setup), 10.seconds)
  }

  "UserRepository" should {

    "create a new user" in {
      val now = LocalDateTime.now()
      val newUser = User(None, "Bob", "bob@example.com", "secret", None, Some(1), now, now)

      val created = Await.result(userRepository.create(newUser), 5.seconds)

      created.id mustBe defined
      created.name mustBe "Bob"
    }

    "find user by email" in {
      val result = Await.result(userRepository.findByEmail("user1@test.com"), 5.seconds)

      result mustBe defined
      result.get.name mustBe "test user 1"
    }

    "find user by id" in {
      val result = Await.result(userRepository.findById(1), 5.seconds)

      result mustBe defined
      result.get.email mustBe "user1@test.com"
    }

    "update user" in {
      val now = LocalDateTime.now()
      val updatedUser = User(Some(1), "Updated user", "user1@test.com", "newpass", None, Some(1), now, now)

      val updatedCount = Await.result(userRepository.update(updatedUser), 5.seconds)

      updatedCount mustBe 1

      val result = Await.result(userRepository.findById(1), 5.seconds)
      result.get.name mustBe "Updated user"
    }

  }
}
