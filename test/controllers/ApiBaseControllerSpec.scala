package controllers

import org.scalatestplus.play._
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.ExecutionContext

class ApiBaseControllerSpec extends PlaySpec {

    // Dummy case class to simulate data payload
    case class DummyData(value: String)
    implicit val dummyWrites: Writes[DummyData] = Json.writes[DummyData]

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    val controller = new ApiBaseController(stubControllerComponents())

    "ApiBaseController" should {

        "return apiResult with status OK and data" in {
            val result = controller.apiResult(DummyData("hello"), "Success message")
            status(result) mustBe OK
            contentType(result) mustBe Some("application/json")
            contentAsString(result) must include ("Success message")
            contentAsString(result) must include ("hello")
        }

        "return apiResultNoData with status OK and message" in {
            val result = controller.apiResultNoData("No data success")
            status(result) mustBe OK
            contentAsString(result) must include ("No data success")
        }

        "return apiSuccess with OK and data" in {
            val result = controller.apiSuccess(DummyData("success-data"), "It worked")
            status(result) mustBe OK
            contentAsString(result) must include ("It worked")
            contentAsString(result) must include ("success-data")
        }

        "return apiSuccessNoData with OK and message" in {
            val result = controller.apiSuccessNoData("No data, still OK")
            status(result) mustBe OK
            contentAsString(result) must include ("No data, still OK")
        }

        "return apiError with BadRequest and error data" in {
            val result = controller.apiError(DummyData("error-data"), "Something went wrong")
            status(result) mustBe BAD_REQUEST
            contentAsString(result) must include ("Something went wrong")
            contentAsString(result) must include ("error-data")
        }

        "return apiErrorNoData with BadRequest and message" in {
            val result = controller.apiErrorNoData("Just an error")
            status(result) mustBe BAD_REQUEST
            contentAsString(result) must include ("Just an error")
        }

        "return apiErrorMessage with BadRequest and message" in {
            val result = controller.apiErrorMessage("Explicit message")
            status(result) mustBe BAD_REQUEST
            contentAsString(result) must include ("Explicit message")
        }

        "return apiResultAsync with Created status" in {
            val future = controller.apiResultAsync(DummyData("async"), "Created!", Created)
            status(future) mustBe CREATED
            contentAsString(future) must include ("Created!")
            contentAsString(future) must include ("async")
        }

        "return apiSuccessAsync with OK" in {
            val future = controller.apiSuccessAsync(DummyData("yay"), "Async success")
            status(future) mustBe OK
            contentAsString(future) must include ("Async success")
            contentAsString(future) must include ("yay")
        }

        "return apiSuccessNoDataAsync with OK" in {
            val future = controller.apiSuccessNoDataAsync("Just async message")
            status(future) mustBe OK
            contentAsString(future) must include ("Just async message")
        }

        "return apiErrorAsync with BadRequest" in {
            val future = controller.apiErrorAsync(DummyData("oops"), "Async error")
            status(future) mustBe BAD_REQUEST
            contentAsString(future) must include ("Async error")
            contentAsString(future) must include ("oops")
        }

        "return apiErrorNoDataAsync with BadRequest" in {
            val future = controller.apiErrorNoDataAsync("Async no data")
            status(future) mustBe BAD_REQUEST
            contentAsString(future) must include ("Async no data")
        }
    }
}
