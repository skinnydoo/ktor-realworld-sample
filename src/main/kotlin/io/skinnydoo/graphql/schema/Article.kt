@file:Suppress("unused")

package io.skinnydoo.graphql.schema

import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.merge
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import io.skinnydoo.articles.GetAllArticlesUseCase
import io.skinnydoo.articles.GetFeedArticlesUseCase
import io.skinnydoo.common.Limit
import io.skinnydoo.common.Offset
import io.skinnydoo.common.Username
import io.skinnydoo.common.models.Article
import io.skinnydoo.common.models.ArticleFeedResult
import io.skinnydoo.common.models.ArticleListResult
import io.skinnydoo.common.models.Tag
import io.skinnydoo.graphql.KtorGraphQLAuthService
import io.skinnydoo.graphql.invoke

data class ArticleListPayload(
  val articles: List<Article>,
  val articlesCount: Int,
) : ArticleListResult, ArticleFeedResult

class ArticleQueryService(
  private val getAllArticles: GetAllArticlesUseCase,
  private val feedArticles: GetFeedArticlesUseCase,
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
}
