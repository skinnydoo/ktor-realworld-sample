package io.skinnydoo.users

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.skinnydoo.common.Email
import io.skinnydoo.common.UserErrors
import io.skinnydoo.common.UserId
import io.skinnydoo.common.UserNotFound
import io.skinnydoo.common.Username
import mu.KotlinLogging
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

private val logger = KotlinLogging.logger {}

interface UserRepository {
  suspend fun userWithId(id: UserId): Either<UserErrors, User>
  suspend fun userWithUsername(username: Username): Either<UserErrors, User>
  suspend fun userWithEmail(email: Email): Either<UserErrors, User>
  suspend fun updateUserDetails(userId: UserId, details: UserUpdateDetails): Either<UserErrors, User>
}

class DefaultUserRepository : UserRepository {

  override suspend fun userWithId(id: UserId): Either<UserErrors, User> = newSuspendedTransaction {
    UserTable.select { UserTable.id eq id.value }
      .map(User.Companion::fromRow)
      .singleOrNull()
      ?.right() ?: UserNotFound().left()
  }

  override suspend fun userWithUsername(username: Username): Either<UserErrors, User> = newSuspendedTransaction {
    UserTable.select { UserTable.username eq username.value }
      .map(User.Companion::fromRow)
      .singleOrNull()
      ?.right() ?: UserNotFound().left()
  }

  override suspend fun userWithEmail(email: Email): Either<UserErrors, User> = newSuspendedTransaction {
    UserTable.select { UserTable.email eq email.value }
      .map(User.Companion::fromRow)
      .singleOrNull()
      ?.right() ?: UserNotFound().left()
  }

  override suspend fun updateUserDetails(userId: UserId, details: UserUpdateDetails): Either<UserErrors, User> {
    newSuspendedTransaction {
      UserTable.update({ UserTable.id eq userId.value }) { row ->
        if (details.email != null) row[email] = details.email.value
        if (details.bio != null) row[bio] = details.bio
        if (details.image != null) row[image] = details.image
      }
    }
    logger.info { "Successfully update user with [RecordID: $userId]" }
    return userWithId(userId)
  }
}
