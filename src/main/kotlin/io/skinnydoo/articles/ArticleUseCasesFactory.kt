package io.skinnydoo.articles

import arrow.core.Either
import io.skinnydoo.articles.tags.Tag
import io.skinnydoo.common.*
import io.skinnydoo.common.models.ArticleErrors
import io.skinnydoo.common.models.CommonErrors
import io.skinnydoo.common.models.ServerError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias AddArticleUseCase = suspend (NewArticle, UserId) -> Either<ServerError, Article>

typealias GetArticleWithSlugUseCase = suspend (Slug, UserId?) -> Either<ArticleErrors, Article>

typealias GetAllArticlesUseCase =
  suspend (UserId?, Tag?, Username?, favoritedBy: Username?, Limit, Offset) -> Either<CommonErrors, List<Article>>

typealias GetFeedArticlesUseCase = suspend (UserId, Limit, Offset) -> Either<CommonErrors, List<Article>>

typealias UpdateArticleUseCase = suspend (Slug, UpdateArticleDetails, UserId) -> Either<ArticleErrors, Article>

typealias DeleteArticleUseCase = suspend (Slug, UserId) -> Either<ArticleErrors, Unit>

typealias FavorArticleUseCase = suspend (Slug, UserId) -> Either<ArticleErrors, Article>

typealias UnFavorArticleUseCase = suspend (Slug, UserId) -> Either<ArticleErrors, Article>

fun addArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): AddArticleUseCase = { article, userId ->
  withContext(dispatcher) { repository.add(article, userId) }
}

fun getArticleWithSlugUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): GetArticleWithSlugUseCase = { slug, userId ->
  withContext(dispatcher) { repository.get(slug, userId) }
}

fun allArticlesUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): GetAllArticlesUseCase = { userId, tag, username, favorited, limit, offset ->
  withContext(dispatcher) { repository.getArticlesFilterBy(tag, username, favorited, userId, limit, offset) }
}

fun getFeedArticlesUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): GetFeedArticlesUseCase = { userId, limit, offset ->
  withContext(dispatcher) { repository.feed(limit, offset, userId) }
}

fun updateArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): UpdateArticleUseCase = { slug, article, userId ->
  withContext(dispatcher) { repository.updateArticle(slug, article, userId) }
}

fun deleteArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): DeleteArticleUseCase = { slug, userId -> withContext(dispatcher) { repository.remove(slug, userId) } }

fun favorArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): FavorArticleUseCase = { slug, userId -> withContext(dispatcher) { repository.favorArticle(slug, userId) } }

fun unFavorArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ArticleRepository,
): FavorArticleUseCase = { slug, userId -> withContext(dispatcher) { repository.unFavorArticle(slug, userId) } }
