package io.skinnydoo.users

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.skinnydoo.common.Email
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import io.skinnydoo.common.models.User
import io.skinnydoo.common.models.UserNotFound
import io.skinnydoo.common.models.UserUpdateDetails
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

interface UserRepository {
  suspend fun userWithId(id: UserId): Either<UserNotFound, User>
  suspend fun userWithUsername(username: Username): Either<UserNotFound, User>
  suspend fun userWithEmail(email: Email): Either<UserNotFound, User>
  suspend fun updateUserDetails(userId: UserId, details: UserUpdateDetails): Either<UserNotFound, User>
}

class DefaultUserRepository(private val userDao: UserDao) : UserRepository {

  override suspend fun userWithId(id: UserId): Either<UserNotFound, User> {
    return userDao.userWithIdOrNull(id)?.right() ?: UserNotFound("User not found").left()
  }

  override suspend fun userWithUsername(username: Username): Either<UserNotFound, User> {
    return userDao.userWithUsername(username)?.right() ?: UserNotFound("User not found").left()
  }

  override suspend fun userWithEmail(email: Email): Either<UserNotFound, User> {
    return userDao.userWithEmail(email)?.right() ?: UserNotFound("User not found").left()
  }

  override suspend fun updateUserDetails(userId: UserId, details: UserUpdateDetails): Either<UserNotFound, User> {
    userDao.updateUserWithId(userId, details)
    logger.info { "Successfully update user with [RecordID: $userId]" }
    return userWithId(userId)
  }
}
