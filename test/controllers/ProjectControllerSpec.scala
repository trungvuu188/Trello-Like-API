package controllers

import dto.response.project.{CompletedProjectSummariesResponse, ProjectSummariesResponse}
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import services.{ProjectService, UserToken}

import scala.concurrent.{ExecutionContext, Future}

class ProjectControllerSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerTest
{

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  implicit lazy val materializer: Materializer = app.materializer

  private def createController(
                                mockService: ProjectService = mock[ProjectService],
                                mockAction: AuthenticatedActionWithUser = mock[AuthenticatedActionWithUser]
                              ): ProjectController = {
    new ProjectController(
      stubMessagesControllerComponents(),
      mockService,
      mockAction
    )
  }

  /** Utility to create AuthenticatedActionWithUser that always injects a UserToken */
  private def mockAuthenticatedAction(userToken: UserToken): AuthenticatedActionWithUser = {
    new AuthenticatedActionWithUser(null, null, null) {
      override def invokeBlock[A](request: play.api.mvc.Request[A],
                                  block: AuthenticatedRequest[A] => scala.concurrent.Future[play.api.mvc.Result]) = {
        block(AuthenticatedRequest(userToken, request))
      }
    }
  }

  "create" should {
//    "return 201 Created when project is created successfully" in {
//      val mockService = mock[ProjectService]
//      when(mockService.createProject(any(), any(), any()))
//        .thenReturn(Future.successful(123))
//
//      val controller = createController(mockService, mockAuthenticatedAction(UserToken(1, "John", "john@example.com")))
//
//      val requestJson = Json.obj("name" -> "New Project", "visibility" -> "public")
//      val request = FakeRequest(POST, "/workspaces/1/projects")
//        .withHeaders("Content-Type" -> "application/json")
//        .withJsonBody(requestJson)
//
//      val result = controller.create(1)(request)
////      println("message " + contentAsString(result))
//
//      status(result) mustBe CREATED
//      (contentAsJson(result) \ "message").as[String] must include("Project created successfully")
//    }

    "return 400 BadRequest when JSON is invalid" in {
      val controller = createController(mock[ProjectService], mockAuthenticatedAction(UserToken(1, "John", "john@example.com")))

      val invalidJson = Json.obj("wrongField" -> "No name")
      val request = FakeRequest(POST, "/workspaces/1/projects")
        .withHeaders(CONTENT_TYPE -> "application/json")
        .withJsonBody(invalidJson)

      val result = controller.create(1)(request)

      status(result) mustBe BAD_REQUEST
    }
  }

  "getAll" should {
    "return 200 OK with project list" in {
      val mockService = mock[ProjectService]
      val projects = Seq(ProjectSummariesResponse(1, "Project A"))
      when(mockService.getProjectsByWorkspaceAndUser(any(), any()))
        .thenReturn(Future.successful(projects))

      val controller = createController(mockService, mockAuthenticatedAction(UserToken(1, "John", "john@example.com")))

      val result = controller.getAll(1)(FakeRequest(GET, "/workspaces/1/projects"))

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Projects retrieved"
      (contentAsJson(result) \ "data").isDefined mustBe true
    }
  }

  "completeProject" should {
    "return 200 OK when project completed" in {
      val mockService = mock[ProjectService]
      when(mockService.completeProject(any(), any()))
        .thenReturn(Future.successful(1))

      val controller = createController(mockService, mockAuthenticatedAction(UserToken(1, "John", "john@example.com")))

      val result = controller.completeProject(1)(FakeRequest(PATCH, "/workspace/projects/1/complete"))

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] must include("Project completed successfully")
    }
  }

  "deleteProject" should {
    "return 200 OK when project deleted" in {
      val mockService = mock[ProjectService]
      when(mockService.deleteProject(any(), any()))
        .thenReturn(Future.successful(1))

      val controller = createController(mockService, mockAuthenticatedAction(UserToken(1, "John", "john@example.com")))

      val result = controller.deleteProject(1)(FakeRequest(PATCH, "/workspace/projects/1/delete"))

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] must include("Project deleted successfully")
    }
  }

  "reopenProject" should {
    "return 200 OK when project reopened" in {
      val mockService = mock[ProjectService]
      when(mockService.reopenProject(any(), any()))
        .thenReturn(Future.successful(1))

      val controller = createController(mockService, mockAuthenticatedAction(UserToken(1, "John", "john@example.com")))

      val result = controller.reopenProject(1)(FakeRequest(PATCH, "/workspace/projects/1/reopen"))

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] must include("Project reopened successfully")
    }
  }

  "getCompletedProjectsByUser" should {
    "return 200 OK with completed projects" in {
      val mockService = mock[ProjectService]
      val completed = Seq(CompletedProjectSummariesResponse(1, "Completed", "Workspace X"))
      when(mockService.getCompletedProjectsByUserId(any()))
        .thenReturn(Future.successful(completed))

      val controller = createController(mockService, mockAuthenticatedAction(UserToken(1, "John", "john@example.com")))

      val result = controller.getCompletedProjectsByUser(FakeRequest(GET, "/workspaces/projects/completed"))

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] must include("Completed projects retrieved")
    }
  }

}
