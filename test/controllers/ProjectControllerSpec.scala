package controllers

import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Configuration}
import services.{JwtService, UserToken}

class ProjectControllerSpec
  extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting
    with ScalaFutures
    with BeforeAndAfterAll {

  // Load test configuration from application.test.conf
  override implicit def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure("config.resource" -> "application.test.conf")
      .build()
  }

  // Get config from app after app init
  lazy val config: Configuration = app.configuration
  lazy val defaultAdminEmail: String =
    config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
  lazy val defaultAdminName: String =
    config.getOptional[String]("admin.name").getOrElse("Administrator")
  lazy val cookieName: String =
    config.getOptional[String]("cookie.name").getOrElse("auth_token")

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
  }
}