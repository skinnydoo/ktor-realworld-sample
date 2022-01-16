package io.skinnydoo.articles

import arrow.core.*
import io.skinnydoo.articles.tags.TagRepository
import io.skinnydoo.common.*
import io.skinnydoo.common.models.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import kotlin.collections.unzip

private val logger = KotlinLogging.logger {}

interface ArticleRepository {
  suspend fun add(article: NewArticle, userId: UserId): Either<ServerError, Article>
  suspend fun get(slug: Slug, userId: UserId?): Either<ArticleNotFound, Article>
  suspend fun remove(slug: Slug, userId: UserId): Either<ArticleErrors, Unit>
  suspend fun getArticlesFilterBy(
    tag: Tag? = null,
    author: Username? = null,
    favoritedBy: Username?,
    userId: UserId? = null,
    limit: Limit = Limit.default,
    offset: Offset = Offset.default,
  ): Either<ServerError, List<Article>>

  suspend fun updateArticle(slug: Slug, details: UpdateArticleDetails, userId: UserId): Either<ArticleErrors, Article>

  /**
   * Get most recent articles from users you follow.
   */
  suspend fun feed(limit: Limit, offset: Offset, userId: UserId): Either<ServerError, List<Article>>

  suspend fun favorArticle(slug: Slug, userId: UserId): Either<ArticleErrors, Article>
  suspend fun unFavorArticle(slug: Slug, userId: UserId): Either<ArticleErrors, Article>
}

class DefaultArticleRepository(
  private val tagRepository: TagRepository,
  private val articleDao: ArticleDao,
  private val articleTagDao: ArticleTagDao,
  private val favoriteArticleDao: FavoriteArticleDao,
) : ArticleRepository {

  override suspend fun add(
    article: NewArticle,
    userId: UserId,
  ): Either<ServerError, Article> = coroutineScope {

    val createTagsJob = article.tagList.map { tag -> async { tagRepository.getOrCreate(tag.value) } }

    val slug = articleDao.insert(article, userId)
    logger.info { "Successfully create new Article [RecordID: $slug]" }

    // create slug & tags mapping
    val (tagIds, _) = createTagsJob.awaitAll().unzip()
    articleTagDao.batchInsert(tagIds, slug)

    articleDao.find(slug, userId)?.right() ?: ServerError().left()
  }

  override suspend fun updateArticle(
    slug: Slug,
    details: UpdateArticleDetails,
    userId: UserId,
  ): Either<ArticleErrors, Article> = articleDao.isSameAuthor(slug, userId)
    .toEither { ArticleNotFound("Article with slug $slug does not exist") }
    .flatMap { same -> if (!same) Forbidden("This operation is not allowed").left() else Unit.right() }
    .tap { articleDao.update(slug, details) }
    .map { articleDao.find(slug, userId) }
    .leftIfNull { ServerError() }

  override suspend fun get(
    slug: Slug,
    userId: UserId?,
  ): Either<ArticleNotFound, Article> = articleDao.find(slug, userId)?.right()
    ?: ArticleNotFound("Article with slug $slug does not exist").left()

  override suspend fun getArticlesFilterBy(
    tag: Tag?,
    author: Username?,
    favoritedBy: Username?,
    userId: UserId?,
    limit: Limit,
    offset: Offset,
  ): Either<ServerError, List<Article>> {
    return Either.catch { articleDao.getArticlesFilterBy(tag, author, favoritedBy, userId, limit, offset) }
      .tapLeft { logger.error(it) { } }
      .mapLeft { ServerError(it.localizedMessage) }
  }

  override suspend fun feed(
    limit: Limit,
    offset: Offset,
    userId: UserId,
  ): Either<ServerError, List<Article>> {
    return Either.catch { articleDao.getArticlesFromFollowedAuthorOnly(userId, limit, offset) }
      .tapLeft { logger.error(it) { } }
      .mapLeft { ServerError(it.localizedMessage) }
  }

  override suspend fun remove(
    slug: Slug,
    userId: UserId,
  ): Either<ArticleErrors, Unit> = articleDao.isSameAuthor(slug, userId)
    .toEither { ArticleNotFound("Article with slug $slug does not exist") }
    .flatMap { same -> if (!same) Forbidden("This operation is not allowed").left() else Unit.right() }
    .tap { articleDao.delete(slug) }

  override suspend fun favorArticle(slug: Slug, userId: UserId): Either<ArticleErrors, Article> {
    return if (articleDao.exists(slug)) {
      favoriteArticleDao.insert(slug, userId)
      logger.info { "Successfully favor article [RecordID: $slug]" }
      get(slug, userId)
    } else ArticleNotFound("Article with slug $slug does not exist").left()
  }

  override suspend fun unFavorArticle(
    slug: Slug,
    userId: UserId,
  ): Either<ArticleErrors, Article> = if (articleDao.exists(slug)) {
    favoriteArticleDao.delete(slug)
    logger.info { "Successfully un-favor article [RecordID: $slug]" }
    get(slug, userId)
  } else ArticleNotFound("Article with slug $slug does not exist").left()
}
