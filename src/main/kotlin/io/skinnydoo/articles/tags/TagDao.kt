package io.skinnydoo.articles.tags

import io.skinnydoo.articles.ArticleTagTable
import io.skinnydoo.articles.tags.TagTable.tag
import io.skinnydoo.common.Slug
import io.skinnydoo.common.db.DatabaseTransactionRunner
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

interface TagDao {
  suspend fun insert(rawValue: String): Pair<TagId, Tag>
  suspend fun getOrNull(rawValue: String): Pair<TagId, Tag>?
  suspend fun tagsForArticle(slug: Slug): List<Tag>
  suspend fun getAll(): List<Tag>
}

class DefaultTagDao(private val transactionRunner: DatabaseTransactionRunner) : TagDao {

  override suspend fun insert(rawValue: String): Pair<TagId, Tag> = transactionRunner {
    TagTable.insertIgnore { it[tag] = rawValue } // insert if not exists
      .let { stmt -> TagId(stmt[TagTable.id].value) to Tag(stmt[tag]) }
  }

  override suspend fun getOrNull(rawValue: String): Pair<TagId, Tag>? = transactionRunner {
    TagTable.select { tag eq rawValue }.singleOrNull()
      ?.let { rr -> TagId(rr[TagTable.id].value) to Tag(rr[tag]) }
  }

  override suspend fun tagsForArticle(slug: Slug): List<Tag> = transactionRunner {
    TagTable.innerJoin(ArticleTagTable, { id }, { tagId })
      .slice(tag)
      .select { ArticleTagTable.articleSlug eq slug.value }
      .map { rr -> Tag(rr[tag]) }
  }

  override suspend fun getAll(): List<Tag> = transactionRunner {
    TagTable.slice(tag).selectAll().map { rr -> Tag(rr[tag]) }
  }
}
