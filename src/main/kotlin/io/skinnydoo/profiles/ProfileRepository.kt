package io.skinnydoo.profiles

import arrow.core.Either
import io.skinnydoo.common.UserErrors
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import io.skinnydoo.common.orFalse
import io.skinnydoo.users.FollowerTable
import io.skinnydoo.users.UserRepository
import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface ProfileRepository {
  suspend fun getUserProfile(username: Username, selfId: UserId?): Either<UserErrors, Profile>
  suspend fun getUserProfile(userId: UserId, selfId: UserId?): Either<UserErrors, Profile>

  suspend fun followUser(username: Username, selfId: UserId): Either<UserErrors, Profile>
  suspend fun followUser(userId: UserId, selfId: UserId): Either<UserErrors, Profile>

  suspend fun unfollowUser(username: Username, selfId: UserId): Either<UserErrors, Profile>
}

class DefaultProfileRepository(private val userRepository: UserRepository) : ProfileRepository {

  override suspend fun getUserProfile(userId: UserId, selfId: UserId?): Either<UserErrors, Profile> {
    return userRepository.userWithId(userId)
      .map { user ->
        val isFollower = selfId?.let { isFollower(it, user.id) }.orFalse()
        Profile.fromUser(user, isFollower)
      }
  }

  override suspend fun getUserProfile(
    username: Username,
    selfId: UserId?,
  ): Either<UserErrors, Profile> = userRepository.userWithUsername(username)
    .map { user ->
      val isFollower = selfId?.let { isFollower(it, user.id) }.orFalse()
      Profile.fromUser(user, isFollower)
    }

  override suspend fun followUser(userId: UserId, selfId: UserId): Either<UserErrors, Profile> {
    return userRepository.userWithId(userId)
      .tap { addFollower(selfId, userId) }
      .map { Profile.fromUser(it, following = true) }
  }

  override suspend fun followUser(username: Username, selfId: UserId): Either<UserErrors, Profile> {
    return userRepository.userWithUsername(username)
      .tap { user -> addFollower(selfId, user.id) }
      .map { Profile.fromUser(it, following = true) }
  }

  override suspend fun unfollowUser(username: Username, selfId: UserId): Either<UserErrors, Profile> {
    return userRepository.userWithUsername(username)
      .tap { removeFollower(selfId, it.id) }
      .map { Profile.fromUser(it, following = false) }
  }

  private suspend fun addFollower(selfId: UserId, otherId: UserId) {
    newSuspendedTransaction {
      FollowerTable.insert {
        it[userId] = selfId.value
        it[followerId] = otherId.value
      }
    }
  }

  private suspend fun removeFollower(selfId: UserId, otherId: UserId) {
    newSuspendedTransaction {
      FollowerTable.deleteWhere {
        FollowerTable.userId eq selfId.value and (FollowerTable.followerId eq otherId.value)
      }
    }
  }

  private suspend fun isFollower(selfId: UserId, otherId: UserId): Boolean {
    return newSuspendedTransaction {
      UserTable
        .join(
          FollowerTable,
          JoinType.INNER,
          onColumn = UserTable.id,
          otherColumn = FollowerTable.userId,
          additionalConstraint = { FollowerTable.followerId eq otherId.value }
        )
        .slice(UserTable.id)
        .select { UserTable.id eq selfId.value }
        .empty()
        .not()
    }
  }
}
