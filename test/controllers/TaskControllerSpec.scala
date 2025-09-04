package controllers

import dto.request.column.CreateColumnRequest
import dto.request.task.{CreateTaskRequest, UpdateTaskRequest}
import exception.AppException
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Configuration}
import services._

class TaskControllerSpec
  extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with ScalaFutures
    with BeforeAndAfterAll {

  override implicit def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "config.resource" -> "application.test.conf",
        "slick.dbs.default.db.url" -> s"jdbc:h2:mem:tasktest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
      )
      .build()
  }

  // config & token
  lazy val config: Configuration = app.configuration
  lazy val defaultAdminEmail: String =
    config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
  lazy val defaultAdminName: String =
    config.getOptional[String]("admin.name").getOrElse("Administrator")
  lazy val cookieName: String =
    config.getOptional[String]("cookie.name").getOrElse("auth_token")

  def fakeToken: String = {
    val jwtService = inject[JwtService]
    jwtService
      .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
      .getOrElse(throw new RuntimeException("JWT token not generated"))
  }

  override def beforeAll(): Unit = {
    val workspaceService = inject[WorkspaceService]
    val projectService = inject[ProjectService]
    val columnService = inject[ColumnService]

    await(
      workspaceService.createWorkspace(
        dto.request.workspace.CreateWorkspaceRequest("Workspace test"),
        1
      )
    )

    await(
      projectService.createProject(
        dto.request.project.CreateProjectRequest("Project test"),
        1,
        1
      )
    )

    await(columnService.createColumn(CreateColumnRequest("Column 1", 1), 1, 1))
  }

  "TaskController" should {

    "create task successfully" in {
      val body = Json.toJson(CreateTaskRequest("Task 1"))
      val request = FakeRequest(POST, "/api/columns/1/tasks")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "message").as[String] must include(
        "Task created successfully"
      )
    }

    "fail when creating task with duplicate name in same column" in {
      val body = Json.toJson(CreateTaskRequest("Task 1"))
      val request = FakeRequest(POST, "/api/columns/1/tasks")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val resultFut = route(app, request).get
      val ex = intercept[AppException] {
        await(resultFut)
      }

      ex.statusCode mustBe CONFLICT
      ex.message must include("Task name already exists in the column")
    }

    "update task successfully" in {
      val body =
        Json.toJson(UpdateTaskRequest("Updated Task", None, None, None, None, None))
      val request = FakeRequest(PATCH, "/api/tasks/1")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)
        .withHeaders(CONTENT_TYPE -> "application/json")

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task updated successfully"
    }

    "fail to update non-existent task" in {
      val nonExistentTaskId = 9999
      val body =
        Json.toJson(UpdateTaskRequest("Some Task", None, None, None, None, None))
      val request = FakeRequest(PATCH, s"/api/tasks/$nonExistentTaskId")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val resultFut = route(app, request).get
      val ex = intercept[AppException] {
        await(resultFut)
      }

      ex.statusCode mustBe NOT_FOUND
      ex.message must include(s"Task with ID $nonExistentTaskId does not exist")
    }

    "get task by id successfully" in {
      val request = FakeRequest(GET, "/api/tasks/1")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task retrieved successfully"
    }

    "archive task successfully" in {
      val request = FakeRequest(PATCH, "/api/tasks/1/archive")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task archived successfully"
    }

    "restore task successfully" in {
      val request = FakeRequest(PATCH, "/api/tasks/1/restore")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task restored successfully"
    }

    "delete task successfully" in {
      val archiveRequest = FakeRequest(PATCH, "/api/tasks/1/archive")
        .withCookies(Cookie(cookieName, fakeToken))
      await(route(app, archiveRequest).get)

      val request = FakeRequest(DELETE, "/api/tasks/1")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task deleted successfully"
    }
  }
}
