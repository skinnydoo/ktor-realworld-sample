package io.skinnydoo.users

import arrow.core.Either
import io.skinnydoo.common.Email
import io.skinnydoo.common.UserErrors
import io.skinnydoo.common.UserNotFound
import io.skinnydoo.common.Username
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

interface UserRepository {
  suspend fun userWithId(id: UUID): Either<UserErrors, User>
  suspend fun userWithUsername(username: Username): Either<UserErrors, User>
  suspend fun userWithEmail(email: Email): Either<UserErrors, User>
  suspend fun updateUserDetails(userId: UUID, details: UserUpdateDetails): Either<UserErrors, User>
}

class DefaultUserRepository : UserRepository {

  override suspend fun userWithId(id: UUID): Either<UserErrors, User> = newSuspendedTransaction {
    val user = UserTable.select { UserTable.id eq id }.map(User.Companion::fromRow).singleOrNull()
    if (user != null) Either.Right(user)
    else Either.Left(UserNotFound())
  }

  override suspend fun userWithUsername(username: Username): Either<UserErrors, User> {
    return newSuspendedTransaction {
      val user = UserTable.select { UserTable.username eq username.value }.map(User.Companion::fromRow).singleOrNull()
      user?.let { Either.Right(it) } ?: Either.Left(UserNotFound())
    }
  }

  override suspend fun userWithEmail(email: Email): Either<UserErrors, User> {
    return newSuspendedTransaction {
      val user = UserTable.select { UserTable.email eq email.value }.map(User.Companion::fromRow).singleOrNull()
      if (user != null) Either.Right(user)
      else Either.Left(UserNotFound())
    }
  }

  override suspend fun updateUserDetails(userId: UUID, details: UserUpdateDetails): Either<UserErrors, User> {
    newSuspendedTransaction {
      UserTable.update({ UserTable.id eq userId }) { row ->
        if (details.email != null) row[email] = details.email.value
        if (details.bio != null) row[bio] = details.bio
        if (details.image != null) row[image] = details.image
      }
    }
    return userWithId(userId)
  }
}
