package controllers

import dto.request.auth.RegisterUserRequest
import dto.response.RegisterUserResponse
import play.api.libs.json.{JsError, JsSuccess, JsValue}

import javax.inject._
import play.api.mvc._
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}

/**
 * Controller responsible for handling authentication-related HTTP requests.
 *
 * @param cc Controller components for Play framework.
 * @param authService Service for authentication operations.
 * @param ec Execution context for asynchronous operations.
 */
class AuthController @Inject()(
                                cc: ControllerComponents,
                                authService: AuthService,
                              )(implicit ec: ExecutionContext) extends ApiBaseController(cc) {

  /**
   * Handles user registration requests.
   *
   * Expects a JSON body matching RegisterUserRequest. Validates the input,
   * delegates user creation to AuthService, and returns appropriate HTTP responses.
   *
   * @return An asynchronous Action that returns a JSON response indicating success or failure.
   */
  def register(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterUserRequest] match {
      case JsSuccess(registerRequest, _) =>
        authService.registerUser(registerRequest).map { user =>
          val responseData = RegisterUserResponse(
            user.id.get,
            user.name,
            user.email
          )
          apiResult(responseData, "User registered successfully", Created)
        }.recover {
          case ex: RuntimeException if ex.getMessage.contains("Email already exists") =>
            apiError(Map("email" -> "Email already exists"), "Registration failed", Conflict)
          case _: Exception =>
            apiError(Map("error" -> "Internal server error"), "Registration failed", InternalServerError)
        }

      case JsError(validationErrors) =>
        val errors = validationErrors.flatMap { case (path, errs) =>
          errs.map { err =>
            val field = path.toString().replaceAll("[\\[\\]/]", "")
            val message = err.message match {
              case "error.minLength" => s"$field is too short"
              case "error.maxLength" => s"$field is too long"
              case "error.email" => s"$field is not a valid email"
              case "error.path.missing" => s"$field is required"
              case other => s"$field: $other"
            }
            message
          }
        }.toList

        Future.successful(apiError(Map("validation_errors" -> errors), "Invalid request data", BadRequest))
    }
  }

}