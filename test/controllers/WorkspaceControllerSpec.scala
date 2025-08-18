package controllers

import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Configuration}
import services.{JwtService, UserToken}

class WorkspaceControllerSpec
    extends PlaySpec
        with GuiceOneAppPerTest
        with Injecting
        with ScalaFutures
        with BeforeAndAfterAll {

    // Get config from app after app init
    lazy val config: Configuration = app.configuration
    lazy val defaultAdminEmail: String =
        config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
    lazy val defaultAdminName: String =
        config.getOptional[String]("admin.name").getOrElse("Administrator")
    lazy val cookieName: String =
        config.getOptional[String]("cookie.name").getOrElse("auth_token")

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

            // Create request with JSON body and cookie
            // Use the workspaceId from the created workspace
            val request = FakeRequest(PUT, s"/api/workspaces/$workspaceId")
                .withJsonBody(updateJson)
                .withCookies(Cookie(cookieName, token))

            // Route the request to the controller
            val result = route(app, request).get

            // Check the response status and content
            status(result) mustBe OK
            (contentAsJson(result) \ "message")
                .as[String] mustBe "Workspace updated successfully"
        }
    }
}
