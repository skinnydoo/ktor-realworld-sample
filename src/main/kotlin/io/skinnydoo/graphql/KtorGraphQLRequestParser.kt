@file:Suppress("BlockingMethodInNonBlockingContext")

package io.skinnydoo.graphql

import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.request.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

/**
 * Logic for how Ktor parses the incoming [ApplicationRequest] into the [GraphQLServerRequest]
 */
class KtorGraphQLRequestParser(private val mapper: ObjectMapper) : GraphQLRequestParser<ApplicationRequest> {

  override suspend fun parseRequest(request: ApplicationRequest): GraphQLServerRequest? = try {
    val rawRequest = request.call.receiveText()
    mapper.readValue(rawRequest, GraphQLServerRequest::class.java)
  } catch (t: Throwable) {
    logger.error(t) { "Unable to parse GraphQL payload." }
    null
  }
}
