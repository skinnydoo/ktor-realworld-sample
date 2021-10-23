@file:OptIn(KtorExperimentalLocationsAPI::class)

package io.skinnydoo.profiles

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.pipeline.PipelineContext
import io.skinnydoo.API_V1
import io.skinnydoo.common.ErrorEnvelope
import io.skinnydoo.common.NotFoundError
import io.skinnydoo.common.Username
import io.skinnydoo.users.User
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

@Location("/profiles/{username}")
data class UserProfileRoute(val username: String)

@Location("/profiles/{username}/follow")
data class FollowUserRoute(val username: String)

fun Route.getUserProfile() {
  val getProfileForUser by inject<GetUserProfileUseCase>()

  authenticate("auth-jwt", optional = true) {
    get<UserProfileRoute> { params ->
      val self = call.principal<User>()
      getProfileForUser(self?.id, Username(params.username))
        .fold(
          ifLeft = { error -> handleError(error) },
          ifRight = { profile -> call.respond(ProfileResponse(profile)) }
        )
    }
  }
}

fun Route.followUser() {
  val followUser by inject<FollowUserUseCase>(named("followUser"))

  authenticate("auth-jwt") {
    post<FollowUserRoute> { params ->
      val self = call.principal<User>()
        ?: return@post call.respond(
          HttpStatusCode.Unauthorized,
          ErrorEnvelope(mapOf("body" to listOf("Unauthorized")))
        )

      followUser(self.id, Username(params.username))
        .fold(
          ifLeft = { error -> handleError(error) },
          ifRight = { call.respond(ProfileResponse(it)) }
        )
    }
  }
}

fun Route.unfollowUser() {
  val unfollowUser by inject<UnfollowUserUseCase>(named("unfollowUser"))

  authenticate("auth-jwt") {
    delete<FollowUserRoute> { params ->
      val self = call.principal<User>()
        ?: return@delete call.respond(
          HttpStatusCode.Unauthorized,
          ErrorEnvelope(mapOf("body" to listOf("Unauthorized")))
        )

      unfollowUser(self.id, Username(params.username))
        .fold(
          ifLeft = { error -> handleError(error) },
          ifRight = { call.respond(ProfileResponse(it)) }
        )
    }
  }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleError(error: NotFoundError) {
  when (error) {
    is NotFoundError.UserNotFound -> {
      val errorBody = ErrorEnvelope(mapOf("body" to listOf("unknown user")))
      call.respond(HttpStatusCode.UnprocessableEntity, errorBody)
    }
  }
}

fun Application.registerProfileRoutes() {
  routing {
    route(API_V1) {
      getUserProfile()
      followUser()
      unfollowUser()
    }
  }
}
