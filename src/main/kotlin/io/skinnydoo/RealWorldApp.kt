package io.skinnydoo

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.serialization.json
import io.skinnydoo.articles.comments.registerCommentRoutes
import io.skinnydoo.articles.registerArticleRoutes
import io.skinnydoo.articles.tags.registerTagsRoutes
import io.skinnydoo.common.*
import io.skinnydoo.profiles.registerProfileRoutes
import io.skinnydoo.users.GetUserWithId
import io.skinnydoo.users.registerUserRoutes
import kotlinx.serialization.json.Json
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level

const val API_V1 = "api/v1"

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
  install(Koin) { configure() }

  val dbFactory by inject<DatabaseFactory> { parametersOf(environment.dbConfig("database")) }
  val json by inject<Json>()
  val jwtService by inject<JwtService> { parametersOf(environment.jwtConfig("jwt")) }
  val getUserWithId by inject<GetUserWithId>(named("getUserWithId"))

  dbFactory.init()

  install(DefaultHeaders) { header("X-Engine", "Ktor") }
  install(CallLogging) {
    level = Level.DEBUG
    filter { call -> call.request.path().startsWith("/") }
  }
  install(ContentNegotiation) { json(json) }
  install(StatusPages) { configure() }
  install(Locations)

  authentication { configure(jwtService) { userId -> getUserWithId(userId).orNull() } }

  registerUserRoutes()
  registerProfileRoutes()
  registerArticleRoutes()
  registerCommentRoutes()
  registerTagsRoutes()
}
