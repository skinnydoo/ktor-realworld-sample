package io.skinnydoo.articles

import io.skinnydoo.articles.tags.TagTable
import io.skinnydoo.common.now
import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime
import java.util.UUID

object ArticleTable : Table(name = "articles") {
  val slug: Column<UUID> = uuid("slug").autoGenerate()
  val title = varchar("title", 255)
  val description = varchar("description", 140).default("")
  val body = text("body")
  val authorId = reference("author_id", UserTable)
  val createAt: Column<LocalDateTime> = datetime("created_at").default(now())
  val updatedAt = datetime("updated_at").default(now())

  override val primaryKey by lazy { PrimaryKey(slug) }
}

object ArticleTagTable : Table(name = "article_tags") {
  val articleSlug = reference(
    "article_slug",
    ArticleTable.slug,
    onDelete = ReferenceOption.CASCADE,
    onUpdate = ReferenceOption.CASCADE,
    fkName = "fk_article_tags_article_slug"
  )
  val tagId = reference(
    "tag_id",
    TagTable,
    onDelete = ReferenceOption.CASCADE,
    onUpdate = ReferenceOption.CASCADE,
    fkName = "fk_article_tags_tag_id"
  )

  override val primaryKey by lazy { PrimaryKey(articleSlug, tagId) }
}

object FavoriteArticleTable : Table(name = "article_favorites") {
  val articleSlug = reference(
    "article_slug",
    ArticleTable.slug,
    onDelete = ReferenceOption.CASCADE,
    onUpdate = ReferenceOption.CASCADE,
    fkName = "fk_article_favorites_article_slug"
  )

  val userId = reference(
    "user_id",
    UserTable,
    onDelete = ReferenceOption.CASCADE,
    onUpdate = ReferenceOption.CASCADE,
    fkName = "fk_article_favorites_user_id"
  )

  override val primaryKey by lazy { PrimaryKey(articleSlug, userId) }
}
