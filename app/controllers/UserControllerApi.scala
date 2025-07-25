package controllers

import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.UserService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

class UserControllerApi @Inject()(
                                cc: ControllerComponents,
                                userService: UserService
                              )(implicit ec: ExecutionContext) extends ApiBaseController(cc) {


}
