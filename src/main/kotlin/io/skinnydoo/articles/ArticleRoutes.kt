@file:OptIn(KtorExperimentalLocationsAPI::class)

package io.skinnydoo.articles

import arrow.core.Either
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.skinnydoo.API_V1
import io.skinnydoo.common.ErrorEnvelope
import io.skinnydoo.common.InvalidSlug
import io.skinnydoo.common.Slug
import io.skinnydoo.common.handleErrors
import io.skinnydoo.users.User
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.util.UUID

private const val ARTICLES = "/articles"

@Location(ARTICLES)
class ArticlesRoute

@Location("$ARTICLES/feed")
class ArticleFeedRoute

@Location("$ARTICLES/{slug}")
data class ArticleRoute(val slug: String)

fun Route.createArticle() {
  val addArticle by inject<AddArticleUseCase>(named("addArticle"))

  authenticate("auth-jwt") {
    post<ArticlesRoute> {
      val body = call.receive<CreateArticleRequest>().article

      val self = call.principal<User>()
        ?: return@post call.respond(
          HttpStatusCode.Unauthorized,
          ErrorEnvelope(mapOf("body" to listOf("Unauthorized")))
        )

      addArticle(body, self.id)
        .map { ArticleResponse(it) }
        .fold({ handleErrors(it) }) { call.respond(it) }
    }
  }
}

fun Route.getArticleWithSlug() {
  val getArticleWithSlug by inject<GetArticleWithSlugUseCase>(named("getArticle"))

  authenticate("auth-jwt", optional = true) {
    get<ArticleRoute> { params ->
      val userId = call.principal<User>()?.id

      Either.catch { UUID.fromString(params.slug) }
        .mapLeft { InvalidSlug(it.localizedMessage) }
        .fold({ handleErrors(it) }) { s ->
          getArticleWithSlug(Slug(s), userId)
            .map { ArticleResponse(it) }
            .fold({ handleErrors(it) }) { call.respond(it) }
        }
    }
  }
}

fun Application.registerArticleRoutes() {
  routing {
    route(API_V1) {
      createArticle()
      getArticleWithSlug()
    }
  }
}
