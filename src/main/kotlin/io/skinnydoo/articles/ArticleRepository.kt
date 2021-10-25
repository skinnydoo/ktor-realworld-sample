package io.skinnydoo.articles

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.skinnydoo.articles.tags.TagRepository
import io.skinnydoo.common.ArticleErrors
import io.skinnydoo.common.ArticleNotFound
import io.skinnydoo.common.AuthorNotFound
import io.skinnydoo.common.ServerError
import io.skinnydoo.common.extensions.orFalse
import io.skinnydoo.profiles.ProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface ArticleRepository {
  suspend fun addArticle(article: NewArticle, selfId: UUID): Either<ServerError, Article>
  suspend fun articleWithSlug(slug: UUID, selfId: UUID?): Either<ArticleErrors, Article>
}

class DefaultArticleRepository(
  private val tagRepository: TagRepository,
  private val profileRepository: ProfileRepository,
) : ArticleRepository {

  override suspend fun addArticle(
    article: NewArticle,
    selfId: UUID,
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
        it[authorId] = selfId
      }

      stmt.resultedValues?.firstOrNull()?.let { rr ->
        val slug = rr[ArticleTable.slug]
        ArticleTagTable.batchInsert(tagIds, shouldReturnGeneratedValues = false) { tagId ->
          this[ArticleTagTable.articleSlug] = slug
          this[ArticleTagTable.tagId] = tagId.value
        }

        profileRepository.getUserProfile(rr[ArticleTable.authorId].value, selfId)
          .fold(
            { Either.Left(ServerError()) },
            { Article.fromRow(rr, profile = it, tags, favoritesCount = 0, favorited = false).right() }
          )
      } ?: Either.Left(ServerError())
    }
  }

  override suspend fun articleWithSlug(
    slug: UUID,
    selfId: UUID?,
  ): Either<ArticleErrors, Article> = newSuspendedTransaction {
    val tags = tagRepository.tagsForArticleWithSlug(slug)
    val favorited = selfId?.let { isFavoritedArticle(slug, it) }.orFalse()
    val favoritesCount = getFavoritesCount(slug)

    ArticleTable
      .select { ArticleTable.slug eq slug }
      .singleOrNull()
      ?.let { rr ->
        profileRepository
          .getUserProfile(rr[ArticleTable.authorId].value, selfId)
          .fold(
            { AuthorNotFound.left() },
            { Article.fromRow(rr, profile = it, tags, favoritesCount, favorited).right() })
      } ?: Either.Left(ArticleNotFound(slug))
  }

  private fun isFavoritedArticle(slug: UUID, selfId: UUID): Boolean = FavoriteArticleTable
    .select { FavoriteArticleTable.articleSlug eq slug and (FavoriteArticleTable.userId eq selfId) }
    .empty()
    .not()

  private fun getFavoritesCount(slug: UUID): Long = FavoriteArticleTable
    .select { FavoriteArticleTable.articleSlug eq slug }
    .count()
}
