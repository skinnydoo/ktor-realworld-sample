package io.skinnydoo.users

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.skinnydoo.common.Email
import io.skinnydoo.common.LoginErrors
import io.skinnydoo.common.Password
import io.skinnydoo.common.UserErrors
import io.skinnydoo.common.UserExists
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import io.skinnydoo.common.checkPassword
import io.skinnydoo.profiles.ProfileRepository
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface AuthRepository {
  suspend fun register(username: Username, email: Email, password: Password): Either<UserErrors, User>
  suspend fun login(email: Email, password: Password): Either<LoginErrors, User>
}

class DefaultAuthRepository(
  private val userRepository: UserRepository,
  private val profileRepository: ProfileRepository,
) : AuthRepository {

  override suspend fun register(
    username: Username,
    email: Email,
    password: Password,
  ): Either<UserErrors, User> = newSuspendedTransaction {
    val userExists = UserTable
      .select { UserTable.username eq username.value or (UserTable.email eq email.value) }
      .firstOrNull() != null

    if (userExists) {
      Either.Left(UserExists())
    } else {
      val id = UserTable.insertAndGetId {
        it[UserTable.username] = username.value
        it[UserTable.email] = email.value
        it[UserTable.password] = password.value
      }.let { UserId(it.value) }

      profileRepository.followUser(id, id) // self follower

      val user = UserTable.select { UserTable.id eq id.value }.map(User.Companion::fromRow).single()
      Either.Right(user)
    }
  }

  override suspend fun login(email: Email, password: Password): Either<LoginErrors, User> {
    return when (val result = userRepository.userWithEmail(email)) {
      is Either.Right -> {
        if (checkPassword(password.value, result.value.password)) result.value.right()
        else LoginErrors.PasswordInvalid.left()
      }
      is Either.Left -> LoginErrors.EmailUnknown.left()
    }
  }
}
