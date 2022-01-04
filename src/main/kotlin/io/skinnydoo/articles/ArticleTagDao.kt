package io.skinnydoo.articles

import io.skinnydoo.articles.tags.TagId
import io.skinnydoo.common.DatabaseTransactionRunner
import io.skinnydoo.common.Slug
import org.jetbrains.exposed.sql.batchInsert

interface ArticleTagDao {
  suspend fun batchInsert(tagIds: List<TagId>, slug: Slug)
}

class DefaultArticleTagDao(private val transactionRunner: DatabaseTransactionRunner) : ArticleTagDao {

  override suspend fun batchInsert(tagIds: List<TagId>, slug: Slug) {
    transactionRunner {
      ArticleTagTable.batchInsert(tagIds, shouldReturnGeneratedValues = false) { tagId ->
        this[ArticleTagTable.articleSlug] = slug.value
        this[ArticleTagTable.tagId] = tagId.value
      }
    }
  }
}
