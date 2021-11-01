package io.skinnydoo.articles

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.skinnydoo.articles.tags.Tag
import io.skinnydoo.articles.tags.TagRepository
import io.skinnydoo.articles.tags.TagTable
import io.skinnydoo.common.ArticleErrors
import io.skinnydoo.common.ArticleNotFound
import io.skinnydoo.common.AuthorNotFound
import io.skinnydoo.common.CommonErrors
import io.skinnydoo.common.Limit
import io.skinnydoo.common.Offset
import io.skinnydoo.common.ServerError
import io.skinnydoo.common.Slug
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import io.skinnydoo.common.orFalse
import io.skinnydoo.profiles.Profile
import io.skinnydoo.profiles.ProfileRepository
import io.skinnydoo.users.UserTable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface ArticleRepository {
  suspend fun addArticle(article: NewArticle, selfId: UserId): Either<ServerError, Article>
  suspend fun articleWithSlug(slug: Slug, selfId: UserId?): Either<ArticleErrors, Article>
  suspend fun allArticles(
    tag: Tag? = null,
    author: Username? = null,
    favoritedBy: Username?,
    selfId: UserId? = null,
    limit: Limit = Limit.DEFAULT,
    offset: Offset = Offset.DEFAULT,
  ): Either<CommonErrors, List<Article>>
}

class DefaultArticleRepository(
  private val tagRepository: TagRepository,
  private val profileRepository: ProfileRepository,
) : ArticleRepository {

  override suspend fun addArticle(
    article: NewArticle,
    selfId: UserId,
  ): Either<ServerError, Article> = coroutineScope {

    val (tagIds, tags) = article.tagList
      .map { tag -> async { tagRepository.getOrCreate(tag.value) } }
      .awaitAll()
      .unzip()

    newSuspendedTransaction {
      val stmt = ArticleTable.insert {
        it[title] = article.title
        it[description] = article.description
        it[body] = article.body
        it[authorId] = selfId.value
      }

      stmt.resultedValues?.firstOrNull()?.let { rr ->
        val slug = rr[ArticleTable.slug]
        ArticleTagTable.batchInsert(tagIds, shouldReturnGeneratedValues = false) { tagId ->
          this[ArticleTagTable.articleSlug] = slug
          this[ArticleTagTable.tagId] = tagId.value
        }

        profileRepository.getUserProfile(UserId(rr[ArticleTable.authorId].value), selfId)
          .fold(
            { Either.Left(ServerError()) },
            { Article.fromRow(rr, profile = it, tags, favoritesCount = 0, favorited = false).right() }
          )
      } ?: Either.Left(ServerError())
    }
  }

  override suspend fun articleWithSlug(
    slug: Slug,
    selfId: UserId?,
  ): Either<ArticleErrors, Article> = newSuspendedTransaction {
    val tags = tagRepository.tagsForArticleWithSlug(slug)
    val favorited = selfId?.let { isFavoritedArticle(slug, it) }.orFalse()
    val favoritesCount = getFavoritesCount(slug)

    ArticleTable
      .select { ArticleTable.slug eq slug.value }
      .singleOrNull()
      ?.let { rr ->
        profileRepository
          .getUserProfile(UserId(rr[ArticleTable.authorId].value), selfId)
          .fold(
            { AuthorNotFound.left() },
            { Article.fromRow(rr, profile = it, tags, favoritesCount, favorited).right() })
      } ?: Either.Left(ArticleNotFound(slug))
  }

  override suspend fun allArticles(
    tag: Tag?,
    author: Username?,
    favoritedBy: Username?,
    selfId: UserId?,
    limit: Limit,
    offset: Offset,
  ): Either<CommonErrors, List<Article>> = newSuspendedTransaction {
    Either.catch {
      val favoriteCount = FavoriteArticleTable.articleSlug.count().alias("favoriteCount")
      ArticleTable
        .leftJoin(FavoriteArticleTable, { slug }, { articleSlug })
        .innerJoin(UserTable, { ArticleTable.authorId }, { id })
        .slice(listOf(favoriteCount) + ArticleTable.columns + UserTable.columns)
        .selectAll()
        .limit(limit.value, offset.value.toLong())
        .orderBy(ArticleTable.createAt to SortOrder.DESC)
        .groupBy(ArticleTable.slug)
        .apply {
          if (author != null) andWhere { UserTable.username eq author.value }

          favoritedBy?.let { username ->
            UserTable.slice(UserTable.id).select { UserTable.username eq username.value }.singleOrNull()
          }?.let { rr ->
            val userId = rr[UserTable.id]
            andWhere { FavoriteArticleTable.userId eq userId }
          }

          tag?.let { tag -> TagTable.slice(TagTable.id).select { TagTable.tag eq tag.value }.singleOrNull() }
            ?.let { rr ->
              val tagId = rr[TagTable.id]
              adjustColumnSet { innerJoin(ArticleTagTable, { ArticleTable.slug }, { articleSlug }) }
              andWhere { ArticleTagTable.tagId eq tagId }
            }
        }
        .map { rr ->
          val authorId = UserId(rr[ArticleTable.authorId].value)
          val isFollower = selfId?.let { profileRepository.isFollower(it, authorId) }.orFalse()

          val slug = Slug(rr[ArticleTable.slug])
          val tags = tagRepository.tagsForArticleWithSlug(slug)
          val favoritesCount = rr[favoriteCount]
          val favorited = selfId?.let { isFavoritedArticle(slug, it) }.orFalse()

          Article.fromRow(rr, profile = Profile.fromRow(rr, isFollower), tags, favoritesCount, favorited)
        }
    }.mapLeft { ServerError(it.localizedMessage) }
  }

  private fun isFavoritedArticle(slug: Slug, selfId: UserId): Boolean = FavoriteArticleTable
    .select { FavoriteArticleTable.articleSlug eq slug.value and (FavoriteArticleTable.userId eq selfId.value) }
    .empty()
    .not()

  private fun getFavoritesCount(slug: Slug): Long = FavoriteArticleTable
    .select { FavoriteArticleTable.articleSlug eq slug.value }
    .count()
}
