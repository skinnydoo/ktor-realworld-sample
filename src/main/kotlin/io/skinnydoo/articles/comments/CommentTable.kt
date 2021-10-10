package io.skinnydoo.articles.comments

import io.skinnydoo.articles.ArticleTable
import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE

object CommentTable : IntIdTable(name = "comments") {
  val comment = text("comment")
  val authorId = reference("author_id", UserTable, onDelete = CASCADE)
  val articleSlug = reference("article_slug", ArticleTable.slug)
}
