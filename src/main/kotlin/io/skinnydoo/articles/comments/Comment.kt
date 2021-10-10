package io.skinnydoo.articles.comments

import io.skinnydoo.common.serializers.LocalDateTimeSerializer
import io.skinnydoo.profiles.Profile
import kotlinx.serialization.Serializable
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
)
