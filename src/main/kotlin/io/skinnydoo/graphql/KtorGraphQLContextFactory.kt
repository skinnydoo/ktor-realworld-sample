package io.skinnydoo.graphql

import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import io.ktor.auth.*
import io.ktor.request.*
import io.skinnydoo.common.models.User

class KtorGraphQLContextFactory : GraphQLContextFactory<AuthContext, ApplicationRequest> {

  override suspend fun generateContext(request: ApplicationRequest): AuthContext? = null

  override suspend fun generateContextMap(request: ApplicationRequest): Map<*, Any>? {
    val user = request.call.principal<User>() ?: return null
    return mapOf("user" to user)
  }
}
