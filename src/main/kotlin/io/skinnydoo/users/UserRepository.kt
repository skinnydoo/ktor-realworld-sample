package io.skinnydoo.users

import arrow.core.Either
import io.skinnydoo.common.Email
import io.skinnydoo.common.NotFoundError
import io.skinnydoo.common.UserNotFound
import io.skinnydoo.common.Username
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

interface UserRepository {
  suspend fun userWithId(id: UUID): Either<NotFoundError, User>
  suspend fun userWithUsername(username: Username): Either<NotFoundError, User>
  suspend fun userWithEmail(email: Email): Either<NotFoundError, User>
  suspend fun updateUserDetails(userId: UUID, details: UserUpdateDetails): Either<NotFoundError, User>
}

class DefaultUserRepository : UserRepository {

  override suspend fun userWithId(id: UUID): Either<NotFoundError, User> = newSuspendedTransaction {
    val user = UserTable.select { UserTable.id eq id }.map((User)::fromRow).singleOrNull()
    if (user != null) Either.Right(user)
    else Either.Left(UserNotFound())
  }

  override suspend fun userWithUsername(username: Username): Either<NotFoundError, User> {
    return newSuspendedTransaction {
      val user = UserTable.select { UserTable.username eq username.value }.map((User)::fromRow).singleOrNull()
      user?.let { Either.Right(it) } ?: Either.Left(UserNotFound())
    }
  }

  override suspend fun userWithEmail(email: Email): Either<NotFoundError, User> {
    return newSuspendedTransaction {
      val user = UserTable.select { UserTable.email eq email.value }.map((User)::fromRow).singleOrNull()
      if (user != null) Either.Right(user)
      else Either.Left(UserNotFound())
    }
  }

  override suspend fun updateUserDetails(userId: UUID, details: UserUpdateDetails): Either<NotFoundError, User> {
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
