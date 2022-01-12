@file:Suppress("BlockingMethodInNonBlockingContext")

package io.skinnydoo.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Application.registerGraphQLRoute() {
  val mapper by inject<ObjectMapper>()
  val ktorGraphQLServer by inject<KtorGraphQLServer>()

  routing {
    post("graphql") {
      // Execute the query against the schema
      val result = ktorGraphQLServer.execute(call.request)
      if (result != null) {
        val json = mapper.writeValueAsString(result)
        call.respond(json)
      } else {
        call.respond(HttpStatusCode.BadRequest, "Invalid request")
      }
    }
  }
}