package io.skinnydoo.users

import arrow.core.Either
import io.skinnydoo.common.Email
import io.skinnydoo.common.NotFoundError
import io.skinnydoo.common.UserNotFound
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface UserRepository {
  suspend fun userWithId(id: UUID): Either<NotFoundError, User>
  suspend fun userWithEmail(email: Email): Either<NotFoundError, User>
}

class DefaultUserRepository : UserRepository {

  override suspend fun userWithId(id: UUID): Either<NotFoundError, User> = newSuspendedTransaction {
    val user = UserTable.select { UserTable.id eq id }.map((User)::fromRow).singleOrNull()
    if (user != null) Either.Right(user)
    else Either.Left(UserNotFound())
  }

  override suspend fun userWithEmail(email: Email): Either<NotFoundError, User> {
    return newSuspendedTransaction {
      val user = UserTable.select { UserTable.email eq email.text }.map((User)::fromRow).singleOrNull()
      if (user != null) Either.Right(user)
      else Either.Left(UserNotFound())
    }
  }
}
