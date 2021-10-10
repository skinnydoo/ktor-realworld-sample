package io.skinnydoo.profiles

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
  val username: String,
  val bio: String,
  val image: String?,
  val following: Boolean,
)
