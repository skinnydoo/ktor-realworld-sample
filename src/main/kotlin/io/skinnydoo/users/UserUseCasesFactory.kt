package io.skinnydoo.users

import arrow.core.Either
import io.skinnydoo.common.UserId
import io.skinnydoo.common.models.User
import io.skinnydoo.common.models.UserNotFound
import io.skinnydoo.common.models.UserUpdateDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias GetUserWithId = suspend (UserId) -> Either<UserNotFound, User>

typealias UpdateUser = suspend (UserId, UserUpdateDetails) -> Either<UserNotFound, User>

fun getUserWithIdUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: UserRepository,
): GetUserWithId = { userId -> withContext(dispatcher) { repository.userWithId(userId) } }

fun updateUserUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: UserRepository,
): UpdateUser = { userId, details ->
  withContext(dispatcher) { repository.updateUserDetails(userId, details) }
}
