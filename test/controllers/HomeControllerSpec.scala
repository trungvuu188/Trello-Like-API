package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec  {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "HomeController GET" should {

    "return 200 OK" in {
      val controller = new HomeController(stubControllerComponents())
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsString(home) must include ("Welcome to the API")
    }
  }
}
