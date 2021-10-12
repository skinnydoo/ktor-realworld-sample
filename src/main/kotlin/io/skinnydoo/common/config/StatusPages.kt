package io.skinnydoo.common.config

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.skinnydoo.common.ErrorEnvelope

fun StatusPages.Configuration.configure() {
  exception<Throwable> { e ->
    call.respond(HttpStatusCode.InternalServerError, ErrorEnvelope(mapOf("body" to listOf(e.toString()))))
  }
}
