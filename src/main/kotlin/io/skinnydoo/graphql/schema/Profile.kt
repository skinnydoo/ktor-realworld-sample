@file:Suppress("unused")

package io.skinnydoo.graphql.schema

import arrow.core.flatMap
import arrow.core.merge
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import io.ktor.http.auth.*
import io.skinnydoo.common.Username
import io.skinnydoo.common.models.ProfileMutationResult
import io.skinnydoo.common.models.ProfileResult
import io.skinnydoo.graphql.KtorGraphQLAuthService
import io.skinnydoo.graphql.invoke
import io.skinnydoo.profiles.FollowUserUseCase
import io.skinnydoo.profiles.GetUserProfileUseCase
import io.skinnydoo.profiles.UnfollowUserUseCase

class ProfileQueryService(
  private val getUserProfile: GetUserProfileUseCase,
  private val authService: KtorGraphQLAuthService,
) : Query {

  suspend fun profile(username: String, dfe: DataFetchingEnvironment): ProfileResult {
    val user = authService(dfe.graphQlContext.get<HttpAuthHeader?>("auth")).orNull()
    return getUserProfile(user?.id, Username(username)).merge()
  }
}

class ProfileMutationService(
  private val followUserUseCase: FollowUserUseCase,
  private val unfollowUserUseCase: UnfollowUserUseCase,
  private val authService: KtorGraphQLAuthService,
) {

  suspend fun followUser(
    username: String,
    dfe: DataFetchingEnvironment,
  ): ProfileMutationResult = authService(dfe.graphQlContext["auth"])
    .flatMap { user -> followUserUseCase(user.id, Username(username)) }
    .merge()

  suspend fun unfollowUser(
    username: String,
    dfe: DataFetchingEnvironment,
  ): ProfileMutationResult = authService(dfe.graphQlContext["auth"])
    .flatMap { user -> unfollowUserUseCase(user.id, Username(username)) }
    .merge()
}
