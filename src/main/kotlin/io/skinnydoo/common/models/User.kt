package io.skinnydoo.common.models

import io.ktor.auth.*
import io.skinnydoo.common.Email
import io.skinnydoo.common.Password
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import io.skinnydoo.users.UserTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

data class User(
  val id: UserId,
  val email: String,
  val password: String,
  val username: String,
  val bio: String,
  val image: String?,
) : Principal {

  companion object {
    fun fromRow(row: ResultRow): User = User(id = UserId(row[UserTable.id].value),
      email = row[UserTable.email],
      password = row[UserTable.password],
      username = row[UserTable.username],
      bio = row[UserTable.bio],
      image = row[UserTable.image])
  }
}

@Serializable
data class LoggedInUser(
  val email: String,
  val token: String,
  val username: String,
  val bio: String,
  val image: String,
) : LoginResult, RegisterResult {

  companion object {
    fun fromUser(user: User, token: String): LoggedInUser {
      return LoggedInUser(email = user.email,
        token = token,
        username = user.username,
        bio = user.bio,
        image = user.image.orEmpty().ifEmpty { "https://picsum.photos/300" }
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
data class UserUpdateRequest(val user: UserUpdateDetails)

@Serializable
data class UserUpdateDetails(val email: Email? = null, val bio: String? = null, val image: String? = null)

@Serializable
data class UserResponse(val user: LoggedInUser)
