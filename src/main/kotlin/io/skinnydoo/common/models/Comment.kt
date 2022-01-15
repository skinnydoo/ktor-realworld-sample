package io.skinnydoo.common.models

import com.expediagroup.graphql.generator.annotations.GraphQLType
import io.skinnydoo.articles.comments.CommentTable
import io.skinnydoo.common.serializers.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDateTime

@Serializable
data class Comment(
  val id: Int,
  val body: String,
  val author: Profile,
  @GraphQLType("DateTime")
  @Serializable(LocalDateTimeSerializer::class)
  val createdAt: LocalDateTime,
  @GraphQLType("DateTime")
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
data class NewComment(@SerialName("body") val text: String)

@Serializable
data class CreateCommentRequest(val comment: NewComment)

@Serializable
data class CommentResponse(val comment: Comment)

@Serializable
data class CommentsResponse(val comments: List<Comment>)
