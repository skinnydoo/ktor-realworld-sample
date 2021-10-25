package io.skinnydoo.profiles

import arrow.core.Either
import io.skinnydoo.common.UserErrors
import io.skinnydoo.common.Username
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID

typealias GetUserProfileUseCase =
  suspend (selfId: UUID?, username: Username) -> Either<UserErrors, Profile>

typealias FollowUserUseCase =
  suspend (selfId: UUID, username: Username) -> Either<UserErrors, Profile>

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
