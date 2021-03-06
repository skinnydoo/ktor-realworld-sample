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

private val logger = KotlinLogging.logger {}

interface UserRepository {
  suspend fun userWithId(id: UserId): Either<UserErrors, User>
  suspend fun userWithUsername(username: Username): Either<UserErrors, User>
  suspend fun userWithEmail(email: Email): Either<UserErrors, User>
  suspend fun updateUserDetails(userId: UserId, details: UserUpdateDetails): Either<UserErrors, User>
}

class DefaultUserRepository(private val userDao: UserDao) : UserRepository {

  override suspend fun userWithId(id: UserId): Either<UserErrors, User> {
    return userDao.userWithIdOrNull(id)?.right() ?: UserNotFound().left()
  }

  override suspend fun userWithUsername(username: Username): Either<UserErrors, User> {
    return userDao.userWithUsername(username)?.right() ?: UserNotFound().left()
  }

  override suspend fun userWithEmail(email: Email): Either<UserErrors, User> {
    return userDao.userWithEmail(email)?.right() ?: UserNotFound().left()
  }

  override suspend fun updateUserDetails(userId: UserId, details: UserUpdateDetails): Either<UserErrors, User> {
    userDao.updateUserWithId(userId, details)
    logger.info { "Successfully update user with [RecordID: $userId]" }
    return userWithId(userId)
  }
}
