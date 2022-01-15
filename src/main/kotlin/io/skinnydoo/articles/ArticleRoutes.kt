@file:OptIn(KtorExperimentalLocationsAPI::class)

package io.skinnydoo.articles

import arrow.core.Either
import arrow.core.getOrElse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.skinnydoo.API_V1
import io.skinnydoo.common.*
import io.skinnydoo.common.models.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.util.*

const val ARTICLES = "/articles"

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
data class ArticleRoute(val slug: String) {

  @Location("/comments")
  data class Comments(val parent: ArticleRoute) {

    @Location("/{id}")
    data class Comment(val parent: Comments, val id: Int)
  }

  @Location("/favorite")
  data class Favorite(val parent: ArticleRoute)
}

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
        .fold({ handleErrors(it) }) { call.respond(HttpStatusCode.Created, it) }
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
        .map(::Slug)
        .fold({ handleErrors(it) }) { s ->
          getArticleWithSlug(s, userId)
            .map { ArticleResponse(it) }
            .fold({ handleErrors(it) }) { call.respond(it) }
        }
    }
  }
}

/**
 * Update an article. Auth is required
 */
fun Route.updateArticle() {
  val updateArticle by inject<UpdateArticleUseCase>(named("updateArticle"))

  authenticate("auth-jwt") {
    put<ArticleRoute> { params ->
      val self = call.principal<User>()
        ?: return@put call.respond(HttpStatusCode.Unauthorized, ErrorEnvelope(mapOf("body" to listOf("Unauthorized"))))

      val body = call.receive<UpdateArticleRequest>().article

      Slug.fromString(params.slug)
        .toEither { InvalidSlug() }
        .fold({ handleErrors(it) }) { slug ->
          updateArticle(slug, body, self.id).map { ArticleResponse(it) }
            .fold({ handleErrors(it) }, { call.respond(it) })
        }
    }
  }
}

/**
 * Delete an article. Auth is required
 */
fun Route.deleteArticle() {
  val deleteArticleWithSlug by inject<DeleteArticleUseCase>(named("deleteArticle"))

  authenticate("auth-jwt") {
    delete<ArticleRoute> { params ->
      val user = call.principal<User>()
        ?: return@delete call.respond(
          status = HttpStatusCode.Unauthorized,
          message = ErrorEnvelope(mapOf("body" to listOf("Unauthorized")))
        )

      Slug.fromString(params.slug)
        .toEither { InvalidSlug() }
        .fold({ handleErrors(it) }) { slug ->
          deleteArticleWithSlug(slug, user.id)
            .fold({ handleErrors(it) }, { call.respond(HttpStatusCode.NoContent) })
        }
    }
  }
}

fun Route.favoriteArticle() {
  val favorArticle by inject<FavorArticleUseCase>(named("favorArticle"))

  authenticate("auth-jwt") {
    post<ArticleRoute.Favorite> { params ->
      val user = call.principal<User>()
        ?: return@post call.respond(
          status = HttpStatusCode.Unauthorized,
          message = ErrorEnvelope(mapOf("body" to listOf("Unauthorized")))
        )

      Slug.fromString(params.parent.slug)
        .toEither { InvalidSlug() }
        .fold({ handleErrors(it) }) { slug ->
          favorArticle(slug, user.id).map { ArticleResponse(it) }
            .fold({ handleErrors(it) }, { call.respond(it) })
        }
    }
  }
}

fun Route.unFavoriteArticle() {
  val unFavorArticle by inject<UnFavorArticleUseCase>(named("unFavorArticle"))

  authenticate("auth-jwt") {
    delete<ArticleRoute.Favorite> { params ->
      val user = call.principal<User>()
        ?: return@delete call.respond(
          status = HttpStatusCode.Unauthorized,
          message = ErrorEnvelope(mapOf("body" to listOf("Unauthorized")))
        )

      Slug.fromString(params.parent.slug)
        .toEither { InvalidSlug() }
        .fold({ handleErrors(it) }) { slug ->
          unFavorArticle(slug, user.id).map { ArticleResponse(it) }
            .fold({ handleErrors(it) }, { call.respond(it) })
        }
    }
  }
}

fun Application.registerArticleRoutes() {
  routing {
    route(API_V1) {
      createArticle()
      getArticleWithSlug()
      updateArticle()
      deleteArticle()
      allArticles()
      articleFeed()
      favoriteArticle()
      unFavoriteArticle()
    }
  }
}
