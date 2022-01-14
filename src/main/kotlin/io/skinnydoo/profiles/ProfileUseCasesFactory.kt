package io.skinnydoo.profiles

import arrow.core.Either
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import io.skinnydoo.common.models.Profile
import io.skinnydoo.common.models.UserNotFound
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias GetUserProfileUseCase = suspend (UserId?, Username) -> Either<UserNotFound, Profile>

typealias FollowUserUseCase = suspend (UserId, Username) -> Either<UserNotFound, Profile>

typealias UnfollowUserUseCase = FollowUserUseCase

fun getUserProfileUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ProfileRepository,
): GetUserProfileUseCase = { selfId, username ->
  withContext(dispatcher) { repository.getUserProfile(username, selfId) }
}

fun followUserUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ProfileRepository,
): FollowUserUseCase = { selfId, username ->
  withContext(dispatcher) { repository.followUser(username, selfId) }
}

fun unfollowUserUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: ProfileRepository,
): UnfollowUserUseCase = { selfId, username ->
  withContext(dispatcher) { repository.unfollowUser(username, selfId) }
}
