@file:Suppress("unused")

package io.skinnydoo.graphql.schema

import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.merge
import arrow.core.zip
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import io.skinnydoo.articles.*
import io.skinnydoo.common.Limit
import io.skinnydoo.common.Offset
import io.skinnydoo.common.Slug
import io.skinnydoo.common.Username
import io.skinnydoo.common.models.*
import io.skinnydoo.graphql.KtorGraphQLAuthService
import io.skinnydoo.graphql.invoke

data class ArticleListPayload(
  val articles: List<Article>,
  val articlesCount: Int,
) : ArticleListResult, ArticleFeedResult

data class ArticleDetails(
  val title: String,
  val description: String,
  val body: String,
  val tagList: List<String>,
)

@JvmInline
value class Deleted(val value: Boolean) : DeleteArticleResult

class ArticleQueryService(
  private val getAllArticles: GetAllArticlesUseCase,
  private val feedArticles: GetFeedArticlesUseCase,
  private val getArticleWithSlug: GetArticleWithSlugUseCase,
  private val authService: KtorGraphQLAuthService,
) : Query {

  suspend fun articles(
    @GraphQLDescription("Filter by tag") tag: String? = "",
    @GraphQLDescription("Filter by author (username)") author: String? = "",
    @GraphQLDescription("Filter by favorites of a user (username)") favorited: String? = "",
    limit: Int? = 20,
    offset: Int? = 0,
    dfe: DataFetchingEnvironment,
  ): ArticleListResult {
    val user = authService(dfe.graphQlContext["auth"]).orNull()

    val theTag = tag?.takeUnless { it.isEmpty() }?.let(::Tag)
    val theAuthor = author?.takeUnless { it.isEmpty() }?.let(::Username)
    val favoritedBy = favorited?.takeUnless { it.isEmpty() }?.let(::Username)
    val theLimit = Limit.fromInt(limit ?: 20).getOrElse { Limit.default }
    val theOffset = Offset.fromInt(offset ?: 0).getOrElse { Offset.default }

    return getAllArticles(user?.id, theTag, theAuthor, favoritedBy, theLimit, theOffset)
      .map { ArticleListPayload(it, it.size) }
      .merge()
  }

  suspend fun myFeed(
    limit: Int? = 20,
    offset: Int? = 0,
    dfe: DataFetchingEnvironment,
  ): ArticleFeedResult = authService(dfe.graphQlContext["auth"])
    .flatMap { user ->
      val theLimit = Limit.fromInt(limit ?: 20).getOrElse { Limit.default }
      val theOffset = Offset.fromInt(offset ?: 0).getOrElse { Offset.default }
      feedArticles(user.id, theLimit, theOffset)
    }
    .map { ArticleListPayload(it, it.size) }
    .merge()

  suspend fun article(slug: String, dfe: DataFetchingEnvironment): ArticleResult {
    val user = authService(dfe.graphQlContext["auth"]).orNull()

    return Slug.fromString(slug)
      .toEither { InvalidSlug("Invalid SLUG string: $slug") }
      .flatMap { getArticleWithSlug(it, user?.id) }
      .merge()
  }
}

class ArticleMutationService(
  private val addArticleUseCase: AddArticleUseCase,
  private val updateArticleUseCase: UpdateArticleUseCase,
  private val deleteArticleUseCase: DeleteArticleUseCase,
  private val favoriteArticle: FavorArticleUseCase,
  private val unFavoriteArticle: UnFavorArticleUseCase,
  private val authService: KtorGraphQLAuthService,
) : Mutation {

  suspend fun createArticle(
    details: ArticleDetails,
    dfe: DataFetchingEnvironment,
  ): CreateArticleResult = authService(dfe.graphQlContext["auth"])
    .flatMap { user ->
      val newArticle = NewArticle(title = details.title,
        description = details.description,
        body = details.body,
        tagList = details.tagList.map(::Tag)
      )
      addArticleUseCase(newArticle, user.id)
    }
    .merge()

  suspend fun updateArticle(
    slug: String,
    details: UpdateArticleDetails,
    dfe: DataFetchingEnvironment,
  ): ArticleMutationResult = authService(dfe.graphQlContext["auth"])
    .zip(Slug.fromString(slug).toEither { InvalidSlug("Invalid SLUG string: $slug") })
    .flatMap { (user, slug) ->
      updateArticleUseCase(slug, details, user.id).mapLeft { handleErrors(it) as ArticleMutationResult }
    }
    .merge()

  suspend fun favouriteArticle(
    slug: String,
    dfe: DataFetchingEnvironment,
  ): ArticleMutationResult = authService(dfe.graphQlContext["auth"])
    .zip(Slug.fromString(slug).toEither { InvalidSlug("Invalid SLUG string: $slug") })
    .flatMap { (user, slug) -> favoriteArticle(slug, user.id).mapLeft { handleErrors(it) as ArticleMutationResult } }
    .merge()

  suspend fun unFavouriteArticle(
    slug: String,
    dfe: DataFetchingEnvironment,
  ): ArticleMutationResult = authService(dfe.graphQlContext["auth"])
    .zip(Slug.fromString(slug).toEither { InvalidSlug("Invalid SLUG string: $slug") })
    .flatMap { (user, slug) -> unFavoriteArticle(slug, user.id).mapLeft { handleErrors(it) as ArticleMutationResult } }
    .merge()

  suspend fun deleteArticle(
    slug: String,
    dfe: DataFetchingEnvironment,
  ): DeleteArticleResult = authService(dfe.graphQlContext["auth"])
    .zip(Slug.fromString(slug).toEither { InvalidSlug("Invalid SLUG string: $slug") })
    .flatMap { (user, slug) -> deleteArticleUseCase(slug, user.id).mapLeft { handleErrors(it) as DeleteArticleResult } }
    .map { Deleted(true) }
    .merge()

  private fun handleErrors(error: ArticleErrors) = when (error) {
    is ArticleNotFound -> error
    is Forbidden -> error
    is ServerError -> error
    is CommentNotFound -> ServerError("Something went wrong")
  }
}
