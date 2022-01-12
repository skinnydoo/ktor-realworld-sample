@file:Suppress("unused")

package io.skinnydoo.graphql.schema

import arrow.core.flatMap
import arrow.core.identity
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import io.ktor.http.auth.*
import io.skinnydoo.common.JwtService
import io.skinnydoo.common.models.LoggedInUser
import io.skinnydoo.common.models.SelfQueryResult
import io.skinnydoo.graphql.getUserId
import io.skinnydoo.users.GetUserWithId

class MeQueryService(
  private val userWithId: GetUserWithId,
  private val jwtService: JwtService,
) : Query {

  suspend fun me(dfe: DataFetchingEnvironment): SelfQueryResult {
    return getUserId(dfe.graphQlContext.get<HttpAuthHeader?>("auth"), jwtService)
      .flatMap { userId -> userWithId(userId) }
      .map { LoggedInUser.fromUser(it, token = "") }
      .fold(::identity, ::identity)
  }
}
