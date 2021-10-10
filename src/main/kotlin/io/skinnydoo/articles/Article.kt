package io.skinnydoo.articles

import io.skinnydoo.articles.tags.Tag
import io.skinnydoo.common.serializers.LocalDateTimeSerializer
import io.skinnydoo.profiles.Profile
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Article(
  val slug: String,
  val title: String,
  val description: String,
  val body: String,
  val tagList: List<Tag>,
  val favorited: Boolean,
  val favoriteCounts: Int,
  val author: Profile,
  @Serializable(with = LocalDateTimeSerializer::class)
  val createdAt: LocalDateTime,
  @Serializable(with = LocalDateTimeSerializer::class)
  val updatedAt: LocalDateTime,
)
