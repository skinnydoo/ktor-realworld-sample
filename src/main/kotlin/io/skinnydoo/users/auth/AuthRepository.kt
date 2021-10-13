package io.skinnydoo.users.auth

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.skinnydoo.common.AlreadyExistsError
import io.skinnydoo.common.Email
import io.skinnydoo.common.LoginError
import io.skinnydoo.common.NotFoundError
import io.skinnydoo.common.Password
import io.skinnydoo.common.UserExists
import io.skinnydoo.common.Username
import io.skinnydoo.common.checkPassword
import io.skinnydoo.users.User
import io.skinnydoo.users.UserRepository
import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface AuthRepository {
  suspend fun register(username: Username, email: Email, password: Password): Either<AlreadyExistsError, User>
  suspend fun login(email: Email, password: Password): Either<LoginError, User>
}

class DefaultAuthRepository(private val userRepository: UserRepository) : AuthRepository {

  override suspend fun register(
    username: Username,
    email: Email,
    password: Password,
  ): Either<AlreadyExistsError, User> = newSuspendedTransaction {
    val userExists = UserTable
      .select { UserTable.username eq username.text or (UserTable.email eq email.text) }
      .firstOrNull() != null

    if (userExists) {
      Either.Left(UserExists())
    } else {
      val id = UserTable.insertAndGetId {
        it[UserTable.username] = username.text
        it[UserTable.email] = email.text
        it[UserTable.password] = password.text
      }
      val user = UserTable.select { UserTable.id eq id }.map((User)::fromRow).single()
      Either.Right(user)
    }
  }

  override suspend fun login(email: Email, password: Password): Either<LoginError, User> {
    return when (val result = userRepository.userWithEmail(email)) {
      is Either.Right -> {
        if (checkPassword(password.text, result.value.password)) result.value.right()
        else LoginError.PasswordInvalid.left()
      }
      is Either.Left -> when (result.value) {
        is NotFoundError.UserNotFound -> LoginError.EmailUnknown.left()
      }
    }
  }
}
