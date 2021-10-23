package io.skinnydoo.profiles

import io.skinnydoo.users.User
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
  val username: String,
  val bio: String,
  val image: String?,
  val following: Boolean,
) {
  companion object {

    fun fromUser(user: User, following: Boolean): Profile = Profile(
      username = user.username,
      bio = user.bio,
      image = user.image,
      following
    )
  }
}

@Serializable
data class ProfileResponse(val profile: Profile)
