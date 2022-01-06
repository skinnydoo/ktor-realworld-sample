package io.skinnydoo.articles

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import io.skinnydoo.articles.ArticleTable.slug
import io.skinnydoo.articles.tags.Tag
import io.skinnydoo.articles.tags.TagDao
import io.skinnydoo.articles.tags.TagTable
import io.skinnydoo.common.*
import io.skinnydoo.common.db.DatabaseTransactionRunner
import io.skinnydoo.profiles.Profile
import io.skinnydoo.users.FollowerTable
import io.skinnydoo.users.UserFollowerDao
import io.skinnydoo.users.UserTable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.jetbrains.exposed.sql.*

interface ArticleDao {
  suspend fun insert(article: NewArticle, authorId: UserId): Slug
  suspend fun find(slug: Slug, userId: UserId?): Article?
  suspend fun update(slug: Slug, details: UpdateArticleDetails): Int
  suspend fun isSameAuthor(slug: Slug, authorId: UserId): Option<Boolean>
  suspend fun exists(slug: Slug): Boolean
  suspend fun getArticlesFromFollowedAuthorOnly(userId: UserId, limit: Limit, offset: Offset): List<Article>
  suspend fun getArticlesFilterBy(
    tag: Tag?,
    author: Username?,
    favoritedBy: Username?,
    userId: UserId?,
    limit: Limit,
    offset: Offset,
  ): List<Article>

  suspend fun delete(slug: Slug): Int
}

class DefaultArticleDao(
  private val transactionRunner: DatabaseTransactionRunner,
  private val tagDao: TagDao,
  private val userFollowerDao: UserFollowerDao,
) : ArticleDao {

  private val favoriteCount = FavoriteArticleTable.articleSlug.count().alias("favoriteCount")

  override suspend fun insert(article: NewArticle, authorId: UserId): Slug = transactionRunner {
    val rawSlug = ArticleTable.insert {
      it[title] = article.title
      it[description] = article.description
      it[body] = article.body
      it[ArticleTable.authorId] = authorId.value
    } get slug
    Slug(rawSlug)
  }

  override suspend fun isSameAuthor(slug: Slug, authorId: UserId): Option<Boolean> = transactionRunner {
    ArticleTable.slice(ArticleTable.authorId)
      .select { ArticleTable.slug eq slug.value }
      .singleOrNull()
      ?.let { rr -> rr[ArticleTable.authorId].value == authorId.value }
      ?.some() ?: none()
  }

  override suspend fun exists(slug: Slug): Boolean = transactionRunner {
    ArticleTable.slice(ArticleTable.slug).select { ArticleTable.slug eq slug.value }.isNotEmpty()
  }

  override suspend fun find(slug: Slug, userId: UserId?): Article? = transactionRunner {
    ArticleTable.innerJoin(UserTable, { authorId }, { id })
      .leftJoin(FavoriteArticleTable, { ArticleTable.slug }, { articleSlug })
      .slice(listOf(favoriteCount) + ArticleTable.columns + UserTable.columns)
      .select { ArticleTable.slug eq slug.value }
      .groupBy(ArticleTable.slug)
      .singleOrNull()
      ?.let { rr -> toArticle(rr, userId) }
  }

  override suspend fun getArticlesFromFollowedAuthorOnly(
    userId: UserId,
    limit: Limit,
    offset: Offset,
  ): List<Article> = transactionRunner {
    supervisorScope {
      ArticleTable
        .innerJoin(UserTable, { authorId }, { id })
        .innerJoin(FollowerTable, { UserTable.id }, { followeeId }, { FollowerTable.userId eq userId.value })
        .leftJoin(FavoriteArticleTable, { slug }, { articleSlug })
        .slice(listOf(favoriteCount) + ArticleTable.columns + UserTable.columns)
        .selectAll()
        .limit(limit.value, offset.value.toLong())
        .orderBy(ArticleTable.createAt to SortOrder.DESC)
        .groupBy(slug)
        .map { rr -> async { toArticle(rr, userId) } }
        .awaitAll()
    }
  }

  override suspend fun getArticlesFilterBy(
    tag: Tag?,
    author: Username?,
    favoritedBy: Username?,
    userId: UserId?,
    limit: Limit,
    offset: Offset,
  ): List<Article> = transactionRunner {
    supervisorScope {
      ArticleTable.innerJoin(UserTable, { authorId }, { id })
        .leftJoin(FavoriteArticleTable, { slug }, { articleSlug })
        .slice(listOf(favoriteCount) + ArticleTable.columns + UserTable.columns)
        .selectAll()
        .limit(limit.value, offset.value.toLong())
        .orderBy(ArticleTable.createAt to SortOrder.DESC)
        .groupBy(slug)
        .apply {
          if (author != null) andWhere { UserTable.username eq author.value }

          favoritedBy
            ?.let { un -> UserTable.slice(UserTable.id).select { UserTable.username eq un.value }.singleOrNull() }
            ?.let { rr -> andWhere { FavoriteArticleTable.userId eq rr[UserTable.id] } }

          tag?.let { tag -> TagTable.slice(TagTable.id).select { TagTable.tag eq tag.value }.singleOrNull() }
            ?.let { rr ->
              adjustColumnSet { innerJoin(ArticleTagTable, { slug }, { articleSlug }) }
              andWhere { ArticleTagTable.tagId eq rr[TagTable.id] }
            }
        }
        .map { rr -> async { toArticle(rr, userId) } }
        .awaitAll()
    }
  }

  override suspend fun update(slug: Slug, details: UpdateArticleDetails) = transactionRunner {
    ArticleTable.update({ ArticleTable.slug eq slug.value }) { rr ->
      if (details.title != null) rr[title] = details.title
      if (details.body != null) rr[body] = details.body
      if (details.description != null) rr[description] = details.description
    }
  }

  private fun isFavorited(slug: Slug, userId: UserId): Boolean = FavoriteArticleTable
    .slice(FavoriteArticleTable.articleSlug)
    .select { FavoriteArticleTable.articleSlug eq slug.value and (FavoriteArticleTable.userId eq userId.value) }
    .isNotEmpty()

  override suspend fun delete(slug: Slug) = transactionRunner {
    ArticleTable.deleteWhere { ArticleTable.slug eq slug.value }
  }

  private suspend fun toArticle(rr: ResultRow, userId: UserId?): Article {
    val slug = Slug(rr[slug])
    val favorites = rr[favoriteCount]
    val favorited = userId != null && isFavorited(slug, userId)
    val tags = tagDao.tagsForArticle(slug)

    val authorId = UserId(rr[ArticleTable.authorId].value)
    val following = userId != null && userFollowerDao.isFollowee(userId, authorId)
    return Article.fromRow(rr, author = Profile.fromRow(rr, following), tags, favorites, favorited)
  }
}
