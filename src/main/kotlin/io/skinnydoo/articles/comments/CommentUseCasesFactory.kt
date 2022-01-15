package io.skinnydoo.articles.comments

import arrow.core.Either
import io.skinnydoo.common.CommentId
import io.skinnydoo.common.Slug
import io.skinnydoo.common.UserId
import io.skinnydoo.common.models.ArticleErrors
import io.skinnydoo.common.models.Comment
import io.skinnydoo.common.models.NewComment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias GetCommentsForArticleUseCase = suspend (Slug, UserId?) -> Either<ArticleErrors, List<Comment>>

typealias AddCommentForArticleUseCase = suspend (Slug, UserId, comment: NewComment) -> Either<ArticleErrors, Comment>

typealias RemoveCommentFromArticleUseCase = suspend (Slug, UserId, CommentId) -> Either<ArticleErrors, Unit>

fun getCommentsForArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: CommentRepository,
): GetCommentsForArticleUseCase = { slug, userId -> withContext(dispatcher) { repository.comments(slug, userId) } }

fun addCommentsForArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: CommentRepository,
): AddCommentForArticleUseCase = { slug, userId, text ->
  withContext(dispatcher) { repository.add(slug, text, userId) }
}

fun removeCommentFromArticleUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: CommentRepository,
): RemoveCommentFromArticleUseCase = { slug, userId, commentId ->
  withContext(dispatcher) { repository.remove(slug, commentId, userId) }
}
