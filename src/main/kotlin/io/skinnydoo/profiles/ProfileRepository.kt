package io.skinnydoo.profiles

import arrow.core.Either
import io.skinnydoo.common.UserErrors
import io.skinnydoo.common.Username
import io.skinnydoo.common.extensions.orFalse
import io.skinnydoo.users.FollowerTable
import io.skinnydoo.users.UserRepository
import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface ProfileRepository {
  suspend fun getUserProfile(username: Username, selfId: UUID?): Either<UserErrors, Profile>
  suspend fun getUserProfile(userId: UUID, selfId: UUID?): Either<UserErrors, Profile>

  suspend fun followUser(username: Username, selfId: UUID): Either<UserErrors, Profile>
  suspend fun followUser(userId: UUID, selfId: UUID): Either<UserErrors, Profile>

  suspend fun unfollowUser(username: Username, selfId: UUID): Either<UserErrors, Profile>
}

class DefaultProfileRepository(private val userRepository: UserRepository) : ProfileRepository {

  override suspend fun getUserProfile(userId: UUID, selfId: UUID?): Either<UserErrors, Profile> {
    return userRepository.userWithId(userId)
      .map { user ->
        val isFollower = selfId?.let { isFollower(it, user.id) }.orFalse()
        Profile.fromUser(user, isFollower)
      }
  }

  override suspend fun getUserProfile(
    username: Username,
    selfId: UUID?,
  ): Either<UserErrors, Profile> = userRepository.userWithUsername(username)
    .map { user ->
      val isFollower = selfId?.let { isFollower(it, user.id) }.orFalse()
      Profile.fromUser(user, isFollower)
    }

  override suspend fun followUser(userId: UUID, selfId: UUID): Either<UserErrors, Profile> {
    return userRepository.userWithId(userId)
      .tap { addFollower(selfId, userId) }
      .map { Profile.fromUser(it, following = true) }
  }

  override suspend fun followUser(username: Username, selfId: UUID): Either<UserErrors, Profile> {
    return userRepository.userWithUsername(username)
      .tap { user -> addFollower(selfId, user.id) }
      .map { Profile.fromUser(it, following = true) }
  }

  override suspend fun unfollowUser(username: Username, selfId: UUID): Either<UserErrors, Profile> {
    return userRepository.userWithUsername(username)
      .tap { removeFollower(selfId, it.id) }
      .map { Profile.fromUser(it, following = false) }
  }

  private suspend fun addFollower(selfId: UUID, otherId: UUID) {
    newSuspendedTransaction {
      FollowerTable.insert {
        it[userId] = selfId
        it[followerId] = otherId
      }
    }
  }

  private suspend fun removeFollower(selfId: UUID, otherId: UUID) {
    newSuspendedTransaction {
      FollowerTable.deleteWhere {
        FollowerTable.userId eq selfId and (FollowerTable.followerId eq otherId)
      }
    }
  }

  private suspend fun isFollower(selfId: UUID, otherId: UUID): Boolean {
    return newSuspendedTransaction {
      UserTable
        .join(
          FollowerTable,
          JoinType.INNER,
          onColumn = UserTable.id,
          otherColumn = FollowerTable.userId,
          additionalConstraint = { FollowerTable.followerId eq otherId }
        )
        .slice(UserTable.id)
        .select { UserTable.id eq selfId }
        .empty()
        .not()
    }
  }
}
