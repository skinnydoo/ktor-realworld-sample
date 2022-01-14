@file:Suppress("unused")

package io.skinnydoo.graphql.schema

import arrow.core.flatMap
import arrow.core.merge
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import io.ktor.http.auth.*
import io.skinnydoo.common.Email
import io.skinnydoo.common.models.LoggedInUser
import io.skinnydoo.common.models.SelfQueryResult
import io.skinnydoo.common.models.UpdateSelfResult
import io.skinnydoo.common.models.UserUpdateDetails
import io.skinnydoo.graphql.KtorGraphQLAuthService
import io.skinnydoo.graphql.invoke
import io.skinnydoo.users.UpdateUser

class MeQueryService(private val authService: KtorGraphQLAuthService) : Query {

  suspend fun me(dfe: DataFetchingEnvironment): SelfQueryResult {
    return authService(dfe.graphQlContext.get<HttpAuthHeader?>("auth"))
      .map { LoggedInUser.fromUser(it, token = "") }
      .merge()
  }
}

class SelfMutationService(
  private val updateUser: UpdateUser,
  private val authService: KtorGraphQLAuthService,
) : Mutation {

  suspend fun updateSelf(
    dfe: DataFetchingEnvironment,
    email: String? = null,
    bio: String? = null,
    image: String? = null,
  ): UpdateSelfResult = authService(dfe.graphQlContext.get<HttpAuthHeader?>("auth"))
    .flatMap { user -> updateUser(user.id, UserUpdateDetails(email?.let(::Email), bio, image)) }
    .map { LoggedInUser.fromUser(it, token = "") }
    .merge()
}
