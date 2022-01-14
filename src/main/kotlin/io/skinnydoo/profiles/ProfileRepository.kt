package io.skinnydoo.profiles

import arrow.core.Either
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import io.skinnydoo.common.models.Profile
import io.skinnydoo.common.models.UserNotFound
import io.skinnydoo.users.UserFollowerDao
import io.skinnydoo.users.UserRepository

interface ProfileRepository {
  suspend fun getUserProfile(username: Username, selfId: UserId?): Either<UserNotFound, Profile>
  suspend fun getUserProfile(userId: UserId, selfId: UserId?): Either<UserNotFound, Profile>

  suspend fun followUser(username: Username, selfId: UserId): Either<UserNotFound, Profile>
  suspend fun followUser(userId: UserId, selfId: UserId): Either<UserNotFound, Profile>

  suspend fun unfollowUser(username: Username, selfId: UserId): Either<UserNotFound, Profile>
  suspend fun isFollowee(selfId: UserId, otherId: UserId): Boolean
}

class DefaultProfileRepository(
  private val userRepository: UserRepository,
  private val userFollowerDao: UserFollowerDao,
) : ProfileRepository {

  override suspend fun getUserProfile(userId: UserId, selfId: UserId?): Either<UserNotFound, Profile> {
    return userRepository.userWithId(userId)
      .map { user ->
        val following = selfId != null && isFollowee(selfId, user.id)
        Profile.fromUser(user, following)
      }
  }

  override suspend fun getUserProfile(
    username: Username,
    selfId: UserId?,
  ): Either<UserNotFound, Profile> = userRepository.userWithUsername(username)
    .map { user ->
      val following = selfId != null && isFollowee(selfId, user.id)
      Profile.fromUser(user, following)
    }

  override suspend fun followUser(userId: UserId, selfId: UserId): Either<UserNotFound, Profile> {
    return userRepository.userWithId(userId)
      .tap { userFollowerDao.insert(selfId, userId) }
      .map { Profile.fromUser(it, following = true) }
  }

  override suspend fun followUser(username: Username, selfId: UserId): Either<UserNotFound, Profile> {
    return userRepository.userWithUsername(username)
      .tap { user -> userFollowerDao.insert(selfId, user.id) }
      .map { Profile.fromUser(it, following = true) }
  }

  override suspend fun unfollowUser(username: Username, selfId: UserId): Either<UserNotFound, Profile> {
    return userRepository.userWithUsername(username)
      .tap { userFollowerDao.remove(selfId, it.id) }
      .map { Profile.fromUser(it, following = false) }
  }

  override suspend fun isFollowee(selfId: UserId, otherId: UserId): Boolean =
    userFollowerDao.isFollowee(selfId, otherId)
}
