package io.skinnydoo.articles

import arrow.core.Either
import io.skinnydoo.common.ArticleErrors
import io.skinnydoo.common.ServerError
import io.skinnydoo.common.Slug
import io.skinnydoo.common.UserId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias AddArticleUseCase = suspend (NewArticle, UserId) -> Either<ServerError, Article>

typealias GetArticleWithSlugUseCase = suspend (Slug, UserId?) -> Either<ArticleErrors, Article>

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
  withContext(dispatcher) { repository.articleWithSlug(slug.value, userId) }
}
