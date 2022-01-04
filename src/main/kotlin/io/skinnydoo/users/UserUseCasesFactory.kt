package io.skinnydoo.users

import arrow.core.Either
import io.skinnydoo.common.UserErrors
import io.skinnydoo.common.UserId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias GetUserWithId = suspend (UserId) -> Either<UserErrors, User>

typealias UpdateUser = suspend (UserId, UserUpdateDetails) -> Either<UserErrors, User>

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
