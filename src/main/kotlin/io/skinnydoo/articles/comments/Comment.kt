package io.skinnydoo.articles.comments

import io.skinnydoo.common.serializers.LocalDateTimeSerializer
import io.skinnydoo.profiles.Profile
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDateTime

@Serializable
data class Comment(
  val id: Int,
  val body: String,
  val author: Profile,
  @Serializable(LocalDateTimeSerializer::class)
  val createdAt: LocalDateTime,
  @Serializable(LocalDateTimeSerializer::class)
  val updatedAt: LocalDateTime,
) {

  companion object {
    fun fromRow(rr: ResultRow, author: Profile): Comment {
      return Comment(
        id = rr[CommentTable.id].value,
        body = rr[CommentTable.comment],
        author,
        createdAt = rr[CommentTable.createAt],
        updatedAt = rr[CommentTable.updatedAt]
      )
    }
  }
}

@Serializable
data class CommentResponse(val comment: Comment)
