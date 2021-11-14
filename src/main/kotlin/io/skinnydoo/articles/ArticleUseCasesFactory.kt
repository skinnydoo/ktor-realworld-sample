package io.skinnydoo.articles

import arrow.core.Either
import io.skinnydoo.articles.tags.Tag
import io.skinnydoo.common.ArticleErrors
import io.skinnydoo.common.CommonErrors
import io.skinnydoo.common.Limit
import io.skinnydoo.common.Offset
import io.skinnydoo.common.ServerError
import io.skinnydoo.common.Slug
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias AddArticleUseCase = suspend (NewArticle, UserId) -> Either<ServerError, Article>

typealias GetArticleWithSlugUseCase = suspend (Slug, UserId?) -> Either<ArticleErrors, Article>

typealias GetAllArticlesUseCase =
  suspend (UserId?, Tag?, Username?, favoritedBy: Username?, Limit, Offset) -> Either<CommonErrors, List<Article>>

typealias GetFeedArticlesUseCase = suspend (UserId, Limit, Offset) -> Either<CommonErrors, List<Article>>

fun addArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): AddArticleUseCase = { article, selfId ->
  withContext(dispatcher) { repository.addArticle(article, selfId) }
}

fun getArticleWithSlugUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): GetArticleWithSlugUseCase = { slug, userId ->
  withContext(dispatcher) { repository.articleWithSlug(slug, userId) }
}

fun allArticlesUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): GetAllArticlesUseCase = { userId, tag, username, favorited, limit, offset ->
  withContext(dispatcher) { repository.allArticles(tag, username, favorited, userId, limit, offset) }
}

fun getFeedArticlesUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): GetFeedArticlesUseCase = { userId, limit, offset ->
  withContext(dispatcher) { repository.feed(limit, offset, userId) }
}
