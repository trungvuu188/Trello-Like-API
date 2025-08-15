package controllers

import dto.response.workspace.WorkspaceResponse
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.mvc.Cookie
import play.api.test.Helpers._
import services.WorkspaceService
import play.api.test._
import play.api.{Application, Configuration}
import services.{JwtService, UserToken}

import scala.concurrent.{ExecutionContext, Future}
class WorkspaceControllerSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting
    with ScalaFutures
    with BeforeAndAfterAll {

class WorkspaceControllerSpec extends PlaySpec with MockitoSugar {
  // Get config from app after app init
  lazy val config: Configuration = app.configuration
  lazy val defaultAdminEmail: String =
    config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
  lazy val defaultAdminName: String =
    config.getOptional[String]("admin.name").getOrElse("Administrator")
  lazy val cookieName: String =
    config.getOptional[String]("cookie.name").getOrElse("auth_token")

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    def mkController(workspaceService: WorkspaceService) = {
        new WorkspaceController(
            stubControllerComponents(),
            workspaceService
        )
    }
  // Override config to use H2
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "db.default.driver" -> "org.h2.Driver",
        "db.default.url" -> "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "db.default.username" -> "sa",
        "db.default.password" -> "",
        "play.evolutions.db.default.enabled" -> "true",
        "play.evolutions.db.default.autoApply" -> "true"
      )
      .build()

  "WorkspaceController" should {

        "GET /workspaces - return all workspaces with success response" in {
            val workspaceService = mock[WorkspaceService]
            val controller = mkController(workspaceService)
    "create workspace successfully" in {
      val createJson = Json.obj(
        "name" -> "Test Workspace",
        "description" -> "Integration test"
      )

      // Fake token (create valid token)
      val jwtService = inject[JwtService]
      val token = jwtService
        .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
        .getOrElse(throw new RuntimeException("JWT token not generated"))

      // Create request with JSON body and cookie
      val request = FakeRequest(POST, "/api/workspaces")
        .withJsonBody(createJson)
        .withCookies(Cookie(cookieName, token))

            val workspaceResponses = Seq(
                WorkspaceResponse(
                    id = 1,
                    name = "Workspace 1",
                    desc = Some("Description 1")
                ),
                WorkspaceResponse(
                    id = 2,
                    name = "Workspace 2",
                    desc = None
                )
            )
      // Route the request to the controller
      val result = route(app, request).get

      // Check the response status and content
      status(result) mustBe CREATED
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Workspace created successfully"
    }

    "update workspace successfully" in {
      val updateJson = Json.obj(
        "name" -> "Updated Workspace",
        "description" -> "Updated description"
      )

      // Insert workspace test before update
      val wsService = inject[services.WorkspaceService]
      val workspaceId = await(
        wsService.createWorkspace(
          dto.request.workspace
            .CreateWorkspaceRequest("Init Workspace", Some("desc")),
          1
        )
      )
      // Create valid token
      val jwtService = inject[JwtService]
      val token = jwtService
        .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
        .getOrElse(throw new RuntimeException("JWT token not generated"))

            when(workspaceService.getAllWorkspaces).thenReturn(Future.successful(workspaceResponses))

            val request = FakeRequest(GET, "/workspaces")
            val result = controller.getAllWorkspaces()(request)
      // Create request with JSON body and cookie
      // Use the workspaceId from the created workspace
      val request = FakeRequest(PUT, s"/api/workspaces/$workspaceId")
        .withJsonBody(updateJson)
        .withCookies(Cookie(cookieName, token))

            status(result) mustBe OK
            val body = contentAsJson(result)
            (body \ "message").as[String] mustBe "Workspaces retrieved"
            (body \ "data").as[Seq[WorkspaceResponse]] must have length 2

            verify(workspaceService, times(1)).getAllWorkspaces
        }

        "GET /workspaces - return empty list with success response when no workspaces exist" in {
            val workspaceService = mock[WorkspaceService]
            val controller = mkController(workspaceService)

            when(workspaceService.getAllWorkspaces).thenReturn(Future.successful(Seq.empty))

            val request = FakeRequest(GET, "/workspaces")
            val result = controller.getAllWorkspaces()(request)
      // Route the request to the controller
      val result = route(app, request).get

            status(result) mustBe OK
            val body = contentAsJson(result)
            (body \ "message").as[String] mustBe "Workspaces retrieved"
            (body \ "data").as[Seq[WorkspaceResponse]] mustBe empty

            verify(workspaceService, times(1)).getAllWorkspaces
        }

        "GET /workspaces/:id - return workspace when it exists" in {
            val workspaceService = mock[WorkspaceService]
            val controller = mkController(workspaceService)

            val workspaceResponse = WorkspaceResponse(
                id = 1,
                name = "Test Workspace",
                desc = Some("Test workspace description")
            )

            when(workspaceService.getWorkspaceById(1)).thenReturn(Future.successful(Some(workspaceResponse)))

            val request = FakeRequest(GET, "/workspaces/1")
            val result = controller.getWorkspaceById(1)(request)

            status(result) mustBe OK
            val body = contentAsJson(result)
            (body \ "message").as[String] mustBe "Workspace retrieved ok"
            (body \ "data" \ "id").as[Int] mustBe 1
            (body \ "data" \ "name").as[String] mustBe "Test Workspace"
            (body \ "data" \ "desc").asOpt[String] mustBe Some("Test workspace description")

            verify(workspaceService, times(1)).getWorkspaceById(1)
        }

        "GET /workspaces/:id - return NotFound when workspace does not exist" in {
            val workspaceService = mock[WorkspaceService]
            val controller = mkController(workspaceService)

            when(workspaceService.getWorkspaceById(999)).thenReturn(Future.successful(None))

            val request = FakeRequest(GET, "/workspaces/999")
            val result = controller.getWorkspaceById(999)(request)

            status(result) mustBe NOT_FOUND
            val body = contentAsJson(result)
            (body \ "error").as[String] mustBe "Workspace 999 not found"

            verify(workspaceService, times(1)).getWorkspaceById(999)
        }
      // Check the response status and content
      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Workspace updated successfully"
    }
  }
}

        "DELETE /workspaces/:id - return success when workspace is deleted" in {
            val workspaceService = mock[WorkspaceService]
            val controller = mkController(workspaceService)

            when(workspaceService.deleteWorkspace(1)).thenReturn(Future.successful(true))

            val request = FakeRequest(DELETE, "/workspaces/1")
            val result = controller.deleteWorkspace(1)(request)

            status(result) mustBe OK
            val body = contentAsJson(result)
            (body \ "message").as[String] mustBe "Workspace deleted successfully"
            (body \ "data").toOption mustBe None // successNoData should have no data field

            verify(workspaceService, times(1)).deleteWorkspace(1)
        }

        "DELETE /workspaces/:id - return NotFound when workspace cannot be deleted" in {
            val workspaceService = mock[WorkspaceService]
            val controller = mkController(workspaceService)

            when(workspaceService.deleteWorkspace(999)).thenReturn(Future.successful(false))

            val request = FakeRequest(DELETE, "/workspaces/999")
            val result = controller.deleteWorkspace(999)(request)

            status(result) mustBe NOT_FOUND
            val body = contentAsJson(result)
            (body \ "message").as[String] mustBe "Workspace not found or could not be deleted"
            (body \ "data").toOption mustBe None // errorNoData should have no data field

            verify(workspaceService, times(1)).deleteWorkspace(999)
        }

        "handle service failures gracefully" in {
            val workspaceService = mock[WorkspaceService]
            val controller = mkController(workspaceService)

            when(workspaceService.getAllWorkspaces).thenReturn(Future.failed(new RuntimeException("Service error")))

            val request = FakeRequest(GET, "/workspaces")
            val result = controller.getAllWorkspaces()(request)

            // The controller doesn't handle exceptions, so this should fail
            result.failed.map { exception =>
                exception.getMessage mustBe "Service error"
                verify(workspaceService, times(1)).getAllWorkspaces
            }
        }
    }
}