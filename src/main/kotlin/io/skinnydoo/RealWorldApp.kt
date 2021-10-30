package io.skinnydoo

import arrow.core.flatMap
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.serialization.json
import io.skinnydoo.articles.registerArticleRoutes
import io.skinnydoo.common.DatabaseFactory
import io.skinnydoo.common.JwtService
import io.skinnydoo.common.UserId
import io.skinnydoo.common.configure
import io.skinnydoo.common.dbConfig
import io.skinnydoo.common.jwtConfig
import io.skinnydoo.profiles.registerProfileRoutes
import io.skinnydoo.users.GetUserWithId
import io.skinnydoo.users.registerUserRoutes
import kotlinx.serialization.json.Json
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level

const val API_V1 = "/v1"

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
  val dbFactory by inject<DatabaseFactory> { parametersOf(environment.dbConfig("database")) }
  val json by inject<Json>()

  install(Koin) { configure() }
  dbFactory.init()

  install(DefaultHeaders) { header("X-Engine", "Ktor") }
  install(CallLogging) {
    level = Level.DEBUG
    filter { call -> call.request.path().startsWith("/") }
  }
  install(ContentNegotiation) { json(json) }
  install(StatusPages) { configure() }
  install(Locations)

  val jwtService by inject<JwtService> { parametersOf(environment.jwtConfig("jwt")) }
  val getUserWithId by inject<GetUserWithId>(named("getUserWithId"))
  authentication {
    jwt(name = "auth-jwt") {
      realm = jwtService.realm
      authSchemes("Token")
      verifier(jwtService.verifier)
      validate { credential ->
        val claim = credential.payload.getClaim("id").asString()
        claim?.let { id ->
          UserId.fromString(id)
            .toEither { null }
            .flatMap { getUserWithId(it) }
            .orNull()
        }
      }
    }
  }

  registerUserRoutes()
  registerProfileRoutes()
  registerArticleRoutes()
}
