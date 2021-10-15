@file:OptIn(KtorExperimentalLocationsAPI::class)

package io.skinnydoo.users

import arrow.core.Either
import io.ktor.application.Application
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.application
import io.ktor.routing.route
import io.ktor.routing.routing
import io.skinnydoo.API_V1
import io.skinnydoo.common.AlreadyExistsError
import io.skinnydoo.common.ErrorEnvelope
import io.skinnydoo.common.JwtService
import io.skinnydoo.common.LoginError
import io.skinnydoo.common.NotFoundError
import io.skinnydoo.common.Password
import io.skinnydoo.common.hash
import io.skinnydoo.common.jwtConfig
import io.skinnydoo.users.auth.LoginUser
import io.skinnydoo.users.auth.RegisterUser
import io.skinnydoo.users.usecases.UpdateUser
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.inject

@Location("/users/login")
class UserLoginRoute

@Location("/users")
class UserCreateRoute

@Location("/user")
class UserRoute

/**
 * Register a new user.
 *
 * POST /v1/users
 */
fun Route.createUser() {
  val registerUser by inject<RegisterUser>()
  val jwtService by inject<JwtService> { parametersOf(application.environment.jwtConfig("jwt")) }

  post<UserCreateRoute> {
    val newUser = call.receive<RegisterUserRequest>().user
    val hashedPassword = Password(hash(newUser.password.value))
    val newUserWithHashedPassword = newUser.copy(password = hashedPassword)
    registerUser(RegisterUser.Params(newUserWithHashedPassword))
      .onSuccess { result ->
        when (result) {
          is Either.Right -> {
            val user = result.value
            val token = jwtService.generateToken(user)
            call.respond(HttpStatusCode.Created, UserResponse(LoggedInUser.fromUser(user, token)))
          }
          is Either.Left -> when (result.value) {
            is AlreadyExistsError.UserAlreadyExist -> {
              application.log.error("User already exists")
              call.respond(
                HttpStatusCode.UnprocessableEntity,
                ErrorEnvelope(mapOf("body" to listOf("user exists")))
              )
            }
          }
        }
      }
      .onFailure { e ->
        application.log.error("Failed to register user", e)
        call.respond(HttpStatusCode.InternalServerError, ErrorEnvelope(mapOf("body" to listOf("Error unknown"))))
      }
  }
}

/**
 * Login for existing user.
 *
 * POST v1/users/login
 */
fun Route.loginUser() {
  val loginWithEmail by inject<LoginUser>()
  val jwtService by inject<JwtService> { parametersOf(application.environment.jwtConfig("jwt")) }

  post<UserLoginRoute> {
    val body = call.receive<UserLoginRequest>().user
    loginWithEmail(LoginUser.Params(body))
      .onSuccess { result ->
        when (result) {
          is Either.Right -> {
            val user = result.value
            val token = jwtService.generateToken(user)
            call.respond(UserResponse(LoggedInUser.fromUser(user, token)))
          }
          is Either.Left -> when (result.value) {
            LoginError.PasswordInvalid -> {
              call.respond(
                status = HttpStatusCode.Unauthorized,
                message = ErrorEnvelope(mapOf("body" to listOf("Invalid password")))
              )
            }
            LoginError.EmailUnknown -> {
              call.respond(
                status = HttpStatusCode.Unauthorized,
                message = ErrorEnvelope(mapOf("body" to listOf("Unknown email")))
              )
            }
          }
        }
      }
      .onFailure { e ->
        application.log.error("Failed to login user", e)
        call.respond(HttpStatusCode.InternalServerError, ErrorEnvelope(mapOf("body" to listOf("Error unknown"))))
      }
  }
}

/**
 * Gets the currently logged-in user.
 *
 * GET /v1/user
 */
fun Route.getCurrentUser() {
  authenticate("auth-jwt") {
    get<UserRoute> {
      val loggedInUser = call.principal<User>()
        ?: return@get call.respond(
          HttpStatusCode.Unauthorized,
          ErrorEnvelope(mapOf("body" to listOf("Unknown user")))
        )
      call.respond(UserResponse(LoggedInUser.fromUser(loggedInUser, token = "")))
    }
  }
}

/**
 * Updated user information for current user
 * PUT /v1/user
 */
fun Route.updateUser() {
  val updateUser by inject<UpdateUser>()

  authenticate("auth-jwt") {
    put<UserRoute> {
      val body = call.receive<UserUpdateRequest>().user

      val loggedInUser = call.principal<User>()
        ?: return@put call.respond(
          HttpStatusCode.Unauthorized,
          ErrorEnvelope(mapOf("body" to listOf("Unknown user")))
        )

      updateUser(UpdateUser.Params(loggedInUser.id, body))
        .fold(
          ifLeft = { e ->
            application.log.error("Failed to update user", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorEnvelope(mapOf("body" to listOf(e.localizedMessage))))
          },
          ifRight = { result ->
            when (result) {
              is Either.Left -> when (result.value) {
                is NotFoundError.UserNotFound -> call.respond(
                  HttpStatusCode.UnprocessableEntity,
                  ErrorEnvelope(mapOf("body" to listOf("Something went wrong")))
                )
              }
              is Either.Right -> call.respond(UserResponse(LoggedInUser.fromUser(result.value, token = "")))
            }
          }
        )
    }
  }
}

fun Application.registerUserRoutes() {
  routing {
    route(API_V1) {
      createUser()
      loginUser()
      getCurrentUser()
      updateUser()
    }
  }
}
