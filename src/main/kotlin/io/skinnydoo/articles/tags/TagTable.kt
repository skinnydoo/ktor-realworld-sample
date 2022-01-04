package io.skinnydoo.articles.tags

import org.jetbrains.exposed.dao.id.LongIdTable

object TagTable : LongIdTable("tags") {
  val tag = varchar("tag", 40).uniqueIndex()
}
