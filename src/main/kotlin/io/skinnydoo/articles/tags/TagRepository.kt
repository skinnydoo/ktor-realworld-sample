package io.skinnydoo.articles.tags

import io.skinnydoo.articles.ArticleTagTable
import io.skinnydoo.common.Slug
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface TagRepository {
  suspend fun getOrCreate(name: String): Pair<TagId, Tag>
  suspend fun tagsForArticleWithSlug(slug: Slug): List<Tag>
  suspend fun allTags(): List<Tag>
}

class DefaultTagRepository : TagRepository {

  override suspend fun getOrCreate(name: String): Pair<TagId, Tag> = newSuspendedTransaction {
    val rr = TagTable.select { TagTable.tag eq name }.firstOrNull()
    if (rr != null) {
      TagId(rr[TagTable.id].value) to Tag(rr[TagTable.tag])
    } else {
      val stmt = TagTable.insert { it[tag] = name }
      TagId(stmt[TagTable.id].value) to Tag(stmt[TagTable.tag])
    }
  }

  override suspend fun tagsForArticleWithSlug(slug: Slug): List<Tag> = newSuspendedTransaction {
    TagTable.innerJoin(ArticleTagTable, { TagTable.id }, { tagId })
      .slice(TagTable.tag)
      .select { ArticleTagTable.articleSlug eq slug.value }
      .map { rr -> Tag(rr[TagTable.tag]) }
  }

  override suspend fun allTags(): List<Tag> = newSuspendedTransaction {
    TagTable.selectAll().map { Tag(it[TagTable.tag]) }
  }
}
