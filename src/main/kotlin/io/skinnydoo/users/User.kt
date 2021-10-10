package io.skinnydoo.users

import io.ktor.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class User(
  val email: String,
  val token: String,
  val username: String,
  val bio: String,
  val image: String?,
) : Principal
