package services

import dto.request.auth.RegisterUserRequest
import models.entities.User
import repositories.{RoleRepository, UserRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import com.github.t3hnar.bcrypt._
import dto.response.auth.AuthResponse

/**
 * Service responsible for authentication-related operations.
 *
 * @param userRepository Repository for user data access.
 * @param roleRepository Repository for role data access.
 * @param ec Execution context for asynchronous operations.
 */
@Singleton
class AuthService@Inject()(
                            userRepository: UserRepository,
                            roleRepository: RoleRepository
                          )(implicit ec: ExecutionContext) {

  /**
   * Registers a new user.
   *
   * Checks if the email already exists, hashes the password, assigns the 'user' role,
   * and creates a new user record.
   *
   * @param request The registration request containing user details.
   * @return A Future containing the created User.
   * @throws RuntimeException if the email already exists or the 'user' role is not found.
   */
  def registerUser(request: RegisterUserRequest): Future[User] = {
    // Check if email existed
    userRepository.findByEmail(request.email).flatMap {
      case Some(_) =>
        Future.failed(new RuntimeException("Email already exists"))
      case None =>
        roleRepository.findByRoleName("user").flatMap {
          case Some(role) =>
            val hashedPassword = request.password.bcryptSafeBounded.get

            val newUser = User(
              name = request.name,
              email = request.email,
              password = hashedPassword,
              roleId = role.id
            )

            userRepository.create(newUser)

          case None =>
            Future.failed(new RuntimeException("Role 'user' not found"))
        }
    }
  }

  def authenticateUser(email: String, password: String): Future[Option[UserToken]] = {

    Future.successful {
      // Mock authentication - replace with real database query
      if (email == "user@example.com" && password == "password") {
        Some(UserToken(
          userId = 123,
          name = "John Doe",
          email = email
        ))
      } else if (email == "admin@example.com" && password == "admin") {
        Some(UserToken(
          userId = 456,
          name = "Admin User",
          email = email
        ))
      } else if (email == "jane@example.com" && password == "password123") {
        Some(UserToken(
          userId = 789,
          name = "Jane Smith",
          email = email
        ))
      } else {
        None
      }
    }
  }

//  Convert UserToken to AuthResponse
  def userTokenToAuthResponse(userToken: UserToken): AuthResponse = {
    AuthResponse(
      id = userToken.userId.toInt,
      name = userToken.name,
      email = userToken.email
    )
  }
}
