package io.skinnydoo.articles

import io.skinnydoo.articles.tags.Tag
import io.skinnydoo.common.models.Profile
import io.skinnydoo.common.serializers.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDateTime

@Serializable
data class Article(
  val slug: String,
  val title: String,
  val description: String,
  val body: String,
  val tagList: List<Tag>,
  val favorited: Boolean,
  val favoritesCount: Long,
  val author: Profile,
  @Serializable(with = LocalDateTimeSerializer::class)
  val createdAt: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class)
  val updatedAt: LocalDateTime,
) {

  companion object {
    fun fromRow(
      rr: ResultRow,
      author: Profile,
      tags: List<Tag>,
      favoritesCount: Long,
      favorited: Boolean,
    ): Article {
      return Article(
        slug = rr[ArticleTable.slug].toString(),
        title = rr[ArticleTable.title],
        description = rr[ArticleTable.description],
        body = rr[ArticleTable.body],
        tagList = tags,
        favoritesCount = favoritesCount,
        favorited = favorited,
        author = author,
        createdAt = rr[ArticleTable.createAt],
        updatedAt = rr[ArticleTable.updatedAt]
      )
    }
  }
}

@Serializable
data class NewArticle(
  val title: String,
  val description: String,
  val body: String,
  val tagList: List<Tag>,
)

@Serializable
data class CreateArticleRequest(val article: NewArticle)

@Serializable
data class UpdateArticleDetails(
  val title: String? = null,
  val description: String? = null,
  val body: String? = null,
)

@Serializable
data class UpdateArticleRequest(val article: UpdateArticleDetails)

@Serializable
data class ArticleResponse(val article: Article)

@Serializable
data class ArticleListResponse(val articles: List<Article>, val articlesCount: Int = articles.size)
