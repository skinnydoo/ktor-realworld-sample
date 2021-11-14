@file:OptIn(KtorExperimentalLocationsAPI::class)

package io.skinnydoo.articles

import arrow.core.Either
import arrow.core.getOrElse
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
import io.skinnydoo.articles.tags.Tag
import io.skinnydoo.common.ErrorEnvelope
import io.skinnydoo.common.InvalidSlug
import io.skinnydoo.common.Limit
import io.skinnydoo.common.Offset
import io.skinnydoo.common.Slug
import io.skinnydoo.common.Username
import io.skinnydoo.common.handleErrors
import io.skinnydoo.users.User
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.util.UUID

private const val ARTICLES = "/articles"

@Location(ARTICLES)
data class ArticlesRoute(
  val tag: String = "",
  val author: String = "",
  val favorited: String = "",
  val limit: Int = 20,
  val offset: Int = 0,
)

@Location("$ARTICLES/feed")
data class ArticleFeedRoute(val limit: Int = 20, val offset: Int = 0)

@Location("$ARTICLES/{slug}")
data class ArticleRoute(val slug: String)

/**
 * Get most recent articles globally. Auth is optional.
 */
fun Route.allArticles() {
  val getAllArticles by inject<GetAllArticlesUseCase>(named("allArticles"))

  authenticate("auth-jwt", optional = true) {
    get<ArticlesRoute> { params ->
      val selfId = call.principal<User>()?.id

      val tag = params.tag.ifEmpty { null }?.let(::Tag)
      val favoritedBy = params.favorited.ifEmpty { null }?.let(::Username)
      val username = params.author.ifEmpty { null }?.let(::Username)
      val limit = Limit.fromInt(params.limit).getOrElse { Limit.default }
      val offset = Offset.fromInt(params.offset).getOrElse { Offset.default }

      getAllArticles(selfId, tag, username, favoritedBy, limit, offset)
        .map { ArticleListResponse(it) }
        .fold({ handleErrors(it) }, { call.respond(it) })
    }
  }
}

/**
 * Get most recent articles from users you follow. Auth is required
 */
fun Route.articleFeed() {
  val feedArticles by inject<GetFeedArticlesUseCase>(named("feed"))

  authenticate("auth-jwt") {
    get<ArticleFeedRoute> { params ->
      val self = call.principal<User>()
        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorEnvelope(mapOf("body" to listOf("Unauthorized"))))

      val limit = Limit.fromInt(params.limit).getOrElse { Limit.default }
      val offset = Offset.fromInt(params.offset).getOrElse { Offset.default }
      feedArticles(self.id, limit, offset)
        .map { ArticleListResponse(it) }
        .fold({ handleErrors(it) }, { call.respond(it) })
    }
  }
}

/**
 * Create an article. Auth is required
 */
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

/**
 * Get an article. Auth is optional
 */
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
      allArticles()
      articleFeed()
    }
  }
}
