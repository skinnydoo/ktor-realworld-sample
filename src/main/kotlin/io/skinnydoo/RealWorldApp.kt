package io.skinnydoo

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.serialization.*
import io.skinnydoo.articles.comments.registerCommentRoutes
import io.skinnydoo.articles.registerArticleRoutes
import io.skinnydoo.articles.tags.registerTagsRoutes
import io.skinnydoo.common.JwtService
import io.skinnydoo.common.configure
import io.skinnydoo.common.db.DatabaseFactory
import io.skinnydoo.common.db.dbConfig
import io.skinnydoo.common.jwtConfig
import io.skinnydoo.common.koinModules
import io.skinnydoo.graphql.registerGraphQLRoute
import io.skinnydoo.profiles.registerProfileRoutes
import io.skinnydoo.users.GetUserWithId
import io.skinnydoo.users.registerUserRoutes
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import java.util.*

const val API_V1 = "api/v1"

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module(koinModules: List<Module> = koinModules()) {
  install(Koin) { configure(koinModules) }

  val dbFactory by inject<DatabaseFactory> { parametersOf(environment.dbConfig("ktor.database")) }
  val json by inject<Json>()
  val jwtService by inject<JwtService> { parametersOf(environment.jwtConfig(JwtService.CONFIG_PATH)) }
  val getUserWithId by inject<GetUserWithId>(named("getUserWithId"))

  dbFactory.connect()

  install(DefaultHeaders)
  install(AutoHeadResponse)
  install(CallId) {
    // Attempt to retrieve a callId from the 'X-Request-ID' header
    retrieve { call -> call.request.header(HttpHeaders.XRequestId) }

    // If we can't retrieve a callId,
    // try the 'generate' blocks coalescing until one of them is not null.
    generate { UUID.randomUUID().toString() }

    // Once a callId is generated, verify is called to check if the retrieved or generated callId String is valid.
    verify { callId: String -> callId.isNotEmpty() }

    // Update the response with the callId in the specified headerName
    reply { call, callId -> call.response.header(HttpHeaders.XRequestId, callId) }
  }
  install(CallLogging) {
    callIdMdc("mdc-call-id")
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

@Suppress("unused")
fun Application.graphQLModule() {
  registerGraphQLRoute()
}
