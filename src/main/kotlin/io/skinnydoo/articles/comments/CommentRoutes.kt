package io.skinnydoo.articles.comments

import arrow.core.Either
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.skinnydoo.API_V1
import io.skinnydoo.articles.ArticleRoute
import io.skinnydoo.common.*
import io.skinnydoo.common.models.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.util.*

fun Route.getCommentsForArticle() {
  val commentsForArticle by inject<GetCommentsForArticleUseCase>(named("commentsForArticle"))

  authenticate("auth-jwt", optional = true) {
    get<ArticleRoute.Comments> { params ->
      val userId = call.principal<User>()?.id

      Either.catch { Slug(UUID.fromString(params.parent.slug)) }
        .mapLeft { InvalidSlug(it.localizedMessage) }
        .fold({ handleErrors(it) }) { slug ->
          commentsForArticle(slug, userId).map(::CommentsResponse).fold({ handleErrors(it) }, { call.respond(it) })
        }
    }
  }
}

fun Route.addCommentForArticle() {
  val addComments by inject<AddCommentForArticleUseCase>(named("addComments"))

  authenticate("auth-jwt") {
    post<ArticleRoute.Comments> { params ->
      val userId = call.principal<User>()?.id ?: return@post call.respond(HttpStatusCode.Unauthorized,
        ErrorEnvelope(mapOf("body" to listOf("Unauthorized"))))

      val body = call.receive<CreateCommentRequest>()

      Either.catch { Slug(UUID.fromString(params.parent.slug)) }
        .mapLeft { InvalidSlug(it.localizedMessage) }
        .fold({ handleErrors(it) }) { slug ->
          addComments(slug, userId, body.comment).map { CommentResponse(it) }
            .fold({ handleErrors(it) }, { call.respond(HttpStatusCode.Created, it) })
        }
    }
  }
}

fun Route.removeCommentForArticle() {
  val removeComments by inject<RemoveCommentFromArticleUseCase>(named("removeComments"))

  authenticate("auth-jwt") {
    delete<ArticleRoute.Comments.Comment> { params ->
      val userId = call.principal<User>()?.id ?: return@delete call.respond(HttpStatusCode.Unauthorized,
        ErrorEnvelope(mapOf("body" to listOf("Unauthorized"))))

      Either.catch { Slug(UUID.fromString(params.parent.parent.slug)) }
        .mapLeft { InvalidSlug(it.localizedMessage) }
        .fold({ handleErrors(it) }) { slug ->
          removeComments(slug, userId, CommentId(params.id)).fold({ handleErrors(it) },
            { call.respond(HttpStatusCode.NoContent) })
        }
    }
  }
}

fun Application.registerCommentRoutes() {
  routing {
    route(API_V1) {
      getCommentsForArticle()
      addCommentForArticle()
      removeCommentForArticle()
    }
  }
}
