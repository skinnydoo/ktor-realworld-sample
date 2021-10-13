@file:OptIn(KtorExperimentalLocationsAPI::class)

package io.skinnydoo.users

import arrow.core.Either
import io.ktor.application.Application
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
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
import io.skinnydoo.common.Password
import io.skinnydoo.common.hash
import io.skinnydoo.common.jwtConfig
import io.skinnydoo.users.auth.LoginUser
import io.skinnydoo.users.auth.RegisterUser
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.inject

@Location("/users/login")
class UserLoginRoute

@Location("/users")
class UserCreateRoute

@Location("/user")
class UserRoute

fun Route.createUser() {
  val registerUser by inject<RegisterUser>()
  val jwtService by inject<JwtService> { parametersOf(application.environment.jwtConfig("jwt")) }

  post<UserCreateRoute> {
    val newUser = call.receive<RegisterUserRequest>().user
    val hashedPassword = Password(hash(newUser.password.text))
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
          is Either.Left -> when (val error = result.value) {
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

fun Application.registerUserRoutes() {
  routing {
    route(API_V1) {
      createUser()
      loginUser()
    }
  }
}
