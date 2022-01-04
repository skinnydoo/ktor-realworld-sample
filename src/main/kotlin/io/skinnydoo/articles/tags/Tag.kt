package io.skinnydoo.articles.tags

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Tag(val value: String) {
  init {
    require(value.isNotBlank()) { "Tag cannot be empty" }
  }
}

@JvmInline
@Serializable
value class TagId(val value: Long)

@Serializable
data class TagsResponse(val tags: List<Tag>)
