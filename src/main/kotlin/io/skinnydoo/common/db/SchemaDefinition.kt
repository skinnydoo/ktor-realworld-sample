package io.skinnydoo.common.db

import io.skinnydoo.articles.ArticleTable
import io.skinnydoo.articles.ArticleTagTable
import io.skinnydoo.articles.FavoriteArticleTable
import io.skinnydoo.articles.comments.CommentTable
import io.skinnydoo.articles.tags.TagTable
import io.skinnydoo.users.FollowerTable
import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object SchemaDefinition {

  private val tables: Array<Table> = arrayOf(
    UserTable,
    FollowerTable,
    ArticleTable,
    ArticleTagTable,
    FavoriteArticleTable,
    CommentTable,
    TagTable,
  )

  fun createSchema() {
    transaction { SchemaUtils.create(*tables) }
  }
}
