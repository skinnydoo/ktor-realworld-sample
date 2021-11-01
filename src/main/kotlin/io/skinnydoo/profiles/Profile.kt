package io.skinnydoo.profiles

import io.skinnydoo.users.User
import io.skinnydoo.users.UserTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

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

    fun fromRow(rr: ResultRow, following: Boolean) = Profile(
      username = rr[UserTable.username],
      bio = rr[UserTable.bio],
      image = rr[UserTable.image],
      following
    )
  }
}

@Serializable
data class ProfileResponse(val profile: Profile)
