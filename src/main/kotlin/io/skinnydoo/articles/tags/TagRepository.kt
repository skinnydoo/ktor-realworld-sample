package io.skinnydoo.articles.tags

import arrow.core.Either
import io.skinnydoo.common.ServerError
import io.skinnydoo.common.Slug
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

interface TagRepository {
  suspend fun getOrCreate(rawValue: String): Pair<TagId, Tag>
  suspend fun tagsForArticle(slug: Slug): List<Tag>
  suspend fun getTags(): Either<ServerError, List<Tag>>
}

class DefaultTagRepository(private val tagDao: TagDao) : TagRepository {

  override suspend fun getOrCreate(rawValue: String): Pair<TagId, Tag> =
    tagDao.getOrNull(rawValue) ?: tagDao.insert(rawValue)

  override suspend fun tagsForArticle(slug: Slug): List<Tag> = tagDao.tagsForArticle(slug)

  override suspend fun getTags(): Either<ServerError, List<Tag>> =
    Either.catch { tagDao.getAll() }
      .tapLeft { logger.error(it) { "tagDao.getAll() failed" } }
      .mapLeft { ServerError(it.localizedMessage) }
}
