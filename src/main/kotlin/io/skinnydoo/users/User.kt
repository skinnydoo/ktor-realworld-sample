package io.skinnydoo.users

import io.ktor.auth.Principal
import io.skinnydoo.common.Email
import io.skinnydoo.common.Password
import io.skinnydoo.common.Username
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

data class User(
  val id: UUID,
  val email: String,
  val password: String,
  val username: String,
  val bio: String,
  val image: String?,
) : Principal {

  companion object {
    fun fromRow(row: ResultRow): User = User(
      id = row[UserTable.id].value,
      email = row[UserTable.email],
      password = row[UserTable.password],
      username = row[UserTable.username],
      bio = row[UserTable.bio],
      image = row[UserTable.image]
    )
  }
}

@Serializable
data class LoggedInUser(
  val email: String,
  val token: String,
  val username: String,
  val bio: String,
  val image: String?,
) {

  companion object {
    fun fromUser(user: User, token: String): LoggedInUser {
      return LoggedInUser(
        email = user.email,
        token = token,
        username = user.username,
        bio = user.bio,
        image = user.image
      )
    }
  }
}

@Serializable
data class UserLoginRequest(val user: UserLoginCredentials)

@Serializable
data class UserLoginCredentials(val email: Email, val password: Password)

@Serializable
data class RegisterUserRequest(val user: NewUserCredentials)

@Serializable
data class NewUserCredentials(val username: Username, val email: Email, val password: Password)

@Serializable
data class UserResponse(val user: LoggedInUser)
