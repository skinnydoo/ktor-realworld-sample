package io.skinnydoo.articles.tags

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Tag(val value: String)

@JvmInline
@Serializable
value class TagId(val value: Long)
