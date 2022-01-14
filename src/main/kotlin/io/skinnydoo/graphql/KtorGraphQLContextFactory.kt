package io.skinnydoo.graphql

import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import io.ktor.auth.*
import io.ktor.request.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class KtorGraphQLContextFactory : GraphQLContextFactory<KtorGraphQLAuthService.AuthContext, ApplicationRequest> {

  override suspend fun generateContext(request: ApplicationRequest): KtorGraphQLAuthService.AuthContext? = null

  override suspend fun generateContextMap(request: ApplicationRequest): Map<*, Any>? {
    val authHeader = runCatching { request.parseAuthorizationHeader() }
      .onFailure { logger.trace(it) { "Illegal HTTP auth header" } }
      .getOrNull() ?: return null

    return mapOf("auth" to authHeader)
  }
}
