package io.skinnydoo.articles.comments

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.skinnydoo.articles.ArticleTable
import io.skinnydoo.common.ArticleErrors
import io.skinnydoo.common.ArticleNotFound
import io.skinnydoo.common.CommentId
import io.skinnydoo.common.CommentNotFound
import io.skinnydoo.common.Forbidden
import io.skinnydoo.common.ServerError
import io.skinnydoo.common.Slug
import io.skinnydoo.common.UserId
import io.skinnydoo.common.orFalse
import io.skinnydoo.profiles.Profile
import io.skinnydoo.profiles.ProfileRepository
import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface CommentRepository {
  suspend fun comments(slug: Slug, userId: UserId?): Either<ArticleErrors, List<Comment>>
  suspend fun add(slug: Slug, comment: String, userId: UserId): Either<ArticleErrors, Comment>
  suspend fun remove(slug: Slug, commentId: CommentId, userId: UserId): Either<ArticleErrors, Unit>
}

class DefaultCommentRepository(
  private val profileRepository: ProfileRepository,
) : CommentRepository {

  override suspend fun comments(
    slug: Slug,
    userId: UserId?,
  ): Either<ArticleErrors, List<Comment>> = newSuspendedTransaction {
    ArticleTable.select { ArticleTable.slug eq slug.value }.singleOrNull()
      ?: return@newSuspendedTransaction ArticleNotFound(slug).left()

    CommentTable
      .innerJoin(UserTable, { authorId }, { id }, { CommentTable.articleSlug eq slug.value })
      .selectAll()
      .orderBy(CommentTable.createAt to SortOrder.DESC)
      .map { rr ->
        val authorId = UserId(rr[CommentTable.authorId].value)
        val isFollower = userId?.let { profileRepository.isFollowee(it, authorId) }.orFalse()
        val author = Profile.fromRow(rr, following = isFollower)
        Comment.fromRow(rr, author)
      }.right()
  }

  override suspend fun add(
    slug: Slug,
    comment: String,
    userId: UserId,
  ): Either<ArticleErrors, Comment> = newSuspendedTransaction {
    ArticleTable.select { ArticleTable.slug eq slug.value }.singleOrNull()
      ?: return@newSuspendedTransaction ArticleNotFound(slug).left()

    val stmt = CommentTable.insert {
      it[CommentTable.comment] = comment
      it[authorId] = userId.value
      it[articleSlug] = slug.value
    }

    val rr = stmt.resultedValues?.singleOrNull() ?: return@newSuspendedTransaction ServerError().left()

    val authorId = UserId.fromUUID(rr[CommentTable.authorId].value)
    profileRepository.getUserProfile(authorId, userId)
      .mapLeft { ServerError() }
      .map { Comment.fromRow(rr, author = it) }
  }

  override suspend fun remove(
    slug: Slug,
    commentId: CommentId,
    userId: UserId,
  ): Either<ArticleErrors, Unit> = newSuspendedTransaction {
    ArticleTable.select { ArticleTable.slug eq slug.value }.singleOrNull()
      ?: return@newSuspendedTransaction ArticleNotFound(slug).left()

    val rr = CommentTable.select { CommentTable.id eq commentId.value }.singleOrNull()
      ?: return@newSuspendedTransaction CommentNotFound(commentId).left()

    if (rr[CommentTable.authorId].value != userId.value) return@newSuspendedTransaction Forbidden.left()

    CommentTable.deleteWhere { CommentTable.id eq commentId.value }
    Either.Right(Unit)
  }
}
