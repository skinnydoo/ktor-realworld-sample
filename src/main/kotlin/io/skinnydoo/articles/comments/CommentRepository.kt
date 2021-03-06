package io.skinnydoo.articles.comments

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.skinnydoo.articles.ArticleDao
import io.skinnydoo.common.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

interface CommentRepository {
  suspend fun comments(slug: Slug, userId: UserId?): Either<ArticleErrors, List<Comment>>
  suspend fun add(slug: Slug, comment: NewComment, userId: UserId): Either<ArticleErrors, Comment>
  suspend fun remove(slug: Slug, commentId: CommentId, userId: UserId): Either<ArticleErrors, Unit>
}

class DefaultCommentRepository(
  private val commentsDao: CommentsDao,
  private val articleDao: ArticleDao,
) : CommentRepository {

  override suspend fun comments(
    slug: Slug,
    userId: UserId?,
  ): Either<ArticleErrors, List<Comment>> {
    return if (articleDao.exists(slug)) commentsDao.getAll(slug, userId).right()
    else ArticleNotFound(slug).left()
  }

  override suspend fun add(
    slug: Slug,
    comment: NewComment,
    userId: UserId,
  ): Either<ArticleErrors, Comment> {
    return if (articleDao.exists(slug)) {
      val commentId = commentsDao.insert(slug, userId, comment)
      logger.info { "Successfully create new comment [RecordID: $commentId]" }
      commentsDao.get(commentId, userId)
        .toEither { ServerError() }
    } else ArticleNotFound(slug).left()
  }

  override suspend fun remove(
    slug: Slug,
    commentId: CommentId,
    userId: UserId,
  ): Either<ArticleErrors, Unit> = if (articleDao.exists(slug)) {
    commentsDao.sameAuthor(commentId, userId)
      .toEither { CommentNotFound(commentId) }
      .flatMap { same -> if (!same) Forbidden.left() else Unit.right() }
      .tap { commentsDao.delete(slug, commentId) }
      .tap { logger.info { "Successfully remove comment with [RecordID: $commentId]" } }
  } else ArticleNotFound(slug).left()
}
