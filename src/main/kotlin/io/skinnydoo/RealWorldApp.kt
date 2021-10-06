package io.skinnydoo

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respondText
import io.ktor.serialization.json
import org.koin.core.logger.Level
import org.koin.ktor.ext.Koin
import org.koin.logger.SLF4JLogger

const val API_V1 = "/v1"

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {

  install(Koin) {
    SLF4JLogger(level = Level.DEBUG)
  }
  install(StatusPages) {
    exception<Throwable> { e ->
      call.respondText(e.localizedMessage, status = HttpStatusCode.InternalServerError)
    }
  }
  install(ContentNegotiation) {
    json()
  }
  install(CallLogging) {
    level = org.slf4j.event.Level.INFO
    filter { call -> call.request.path().startsWith("/") }
  }
  install(DefaultHeaders) {
    header("X-Engine", "Ktor") // will send this header with each response
  }
  install(Locations)
}
