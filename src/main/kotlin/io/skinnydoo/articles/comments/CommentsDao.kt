package io.skinnydoo.articles.comments

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.skinnydoo.common.CommentId
import io.skinnydoo.common.Slug
import io.skinnydoo.common.UserId
import io.skinnydoo.common.db.DatabaseTransactionRunner
import io.skinnydoo.common.models.Profile
import io.skinnydoo.users.UserFollowerDao
import io.skinnydoo.users.UserTable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.jetbrains.exposed.sql.*

interface CommentsDao {
  suspend fun get(commentId: CommentId, userId: UserId): Option<Comment>
  suspend fun getAll(slug: Slug, userId: UserId?): List<Comment>
  suspend fun insert(slug: Slug, userId: UserId, comment: NewComment): CommentId
  suspend fun sameAuthor(commentId: CommentId, authorId: UserId): Option<Boolean>
  suspend fun delete(slug: Slug, commentId: CommentId): Int
}

class DefaultCommentsDao(
  private val transactionRunner: DatabaseTransactionRunner,
  private val userFollowerDao: UserFollowerDao,
) : CommentsDao {

  override suspend fun get(commentId: CommentId, userId: UserId): Option<Comment> = transactionRunner {
    Option.catch {
      CommentTable.innerJoin(UserTable, { authorId }, { id })
        .select { CommentTable.id eq commentId.value }
        .single()
        .let { rr -> mapper(rr, userId) }
    }
  }

  override suspend fun getAll(slug: Slug, userId: UserId?): List<Comment> = transactionRunner {
    supervisorScope {
      CommentTable
        .innerJoin(UserTable, { authorId }, { id }, { CommentTable.articleSlug eq slug.value })
        .selectAll()
        .orderBy(CommentTable.createAt to SortOrder.DESC)
        .map { rr -> async { mapper(rr, userId) } }
        .awaitAll()
    }
  }

  override suspend fun insert(slug: Slug, userId: UserId, comment: NewComment): CommentId = transactionRunner {
    val rawId = CommentTable.insertAndGetId {
      it[CommentTable.comment] = comment.text
      it[authorId] = userId.value
      it[articleSlug] = slug.value
    }
    CommentId(rawId.value)
  }

  override suspend fun sameAuthor(commentId: CommentId, authorId: UserId): Option<Boolean> = transactionRunner {
    CommentTable.slice(CommentTable.authorId)
      .select { CommentTable.id eq commentId.value }
      .singleOrNull()
      ?.let { rr -> rr[CommentTable.authorId].value == authorId.value }?.some() ?: none()
  }

  override suspend fun delete(slug: Slug, commentId: CommentId): Int = transactionRunner {
    CommentTable.deleteWhere { CommentTable.id eq commentId.value }
  }

  private suspend fun mapper(rr: ResultRow, userId: UserId?): Comment {
    val following = userId != null && userFollowerDao.isFollowee(userId, UserId(rr[CommentTable.authorId].value))
    val author = Profile.fromRow(rr, following = following)
    return Comment.fromRow(rr, author)
  }
}
