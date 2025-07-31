package controllers

import dto.request.auth.RegisterUserRequest
import models.entities.User
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import services.{AuthService, CookieService, JwtService}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class AuthControllerSpec extends PlaySpec with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private def createController(
                                mockService: AuthService = mock[AuthService]
                              ): (AuthController, AuthService) = {
    val mockJwtService = mock[JwtService]
    val mockCookieService = mock[CookieService]
    val mockAuthenticatedActionWithUser = mock[AuthenticatedActionWithUser]

    val controller = new AuthController(
      mockService,
      stubControllerComponents(),
      mockJwtService,
      mockCookieService,
      mockAuthenticatedActionWithUser
    )

    (controller, mockService)
  }

  "register" should {
    "return 201 Created with valid input" in {
      val (controller, mockService) = createController()
      val requestJson = Json.obj(
        "name" -> "John",
        "email" -> "john@example.com",
        "password" -> "abc123"
      )

      val fakeRequest = FakeRequest(POST, "/register")
        .withBody(requestJson)
        .withHeaders("Content-Type" -> "application/json")

      when(mockService.registerUser(any[RegisterUserRequest]))
        .thenReturn(Future.successful(
          User(Some(1), "John", "john@example.com", "hashed", None, Some(1), LocalDateTime.now(), LocalDateTime.now())
        ))

      val result = controller.register()(fakeRequest)

      status(result) mustBe CREATED
      contentAsString(result) must include("User registered successfully")
    }

    "return 400 BadRequest when input is invalid (missing name)" in {
      val (controller, _) = createController()
      val invalidJson = Json.obj(
        "email" -> "invalid@example.com",
        "password" -> "123" // name missing + password too short
      )

      val fakeRequest = FakeRequest(POST, "/register")
        .withBody(invalidJson)
        .withHeaders("Content-Type" -> "application/json")

      val result = controller.register()(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Invalid request data")
    }

    "return 409 Conflict when email already exists" in {
      val (controller, mockService) = createController()
      val requestJson = Json.obj(
        "name" -> "Jane",
        "email" -> "jane@example.com",
        "password" -> "abc123"
      )

      when(mockService.registerUser(any[RegisterUserRequest]))
        .thenReturn(Future.failed(new RuntimeException("Email already exists")))

      val fakeRequest = FakeRequest(POST, "/register")
        .withBody(requestJson)
        .withHeaders("Content-Type" -> "application/json")

      val result = controller.register()(fakeRequest)

      status(result) mustBe CONFLICT
      contentAsString(result) must include("Email already exists")
    }

    "return 500 InternalServerError when unexpected exception occurs" in {
      val (controller, mockService) = createController()
      val requestJson = Json.obj(
        "name" -> "Test",
        "email" -> "test@example.com",
        "password" -> "abc123"
      )

      when(mockService.registerUser(any[RegisterUserRequest]))
        .thenReturn(Future.failed(new RuntimeException("Some unexpected error")))

      val fakeRequest = FakeRequest(POST, "/register")
        .withBody(requestJson)
        .withHeaders("Content-Type" -> "application/json")

      val result = controller.register()(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) must include("Internal server error")
    }
  }
}
