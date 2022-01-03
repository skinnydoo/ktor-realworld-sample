package io.skinnydoo.users

import io.skinnydoo.common.DatabaseTransactionRunner
import io.skinnydoo.common.UserId
import org.jetbrains.exposed.sql.*

interface UserFollowerDao {
  suspend fun insert(userId: UserId, otherId: UserId)
  suspend fun remove(userId: UserId, otherId: UserId)
  suspend fun isFollowee(userId: UserId, otherId: UserId): Boolean
}

class DefaultUserFollowerDao(private val transactionRunner: DatabaseTransactionRunner) : UserFollowerDao {

  override suspend fun insert(userId: UserId, otherId: UserId) {
    transactionRunner {
      FollowerTable.insert {
        it[FollowerTable.userId] = userId.value
        it[followeeId] = otherId.value
      }
    }
  }

  override suspend fun remove(userId: UserId, otherId: UserId) {
    transactionRunner {
      FollowerTable.deleteWhere {
        FollowerTable.userId eq userId.value and (FollowerTable.followeeId eq otherId.value)
      }
    }
  }

  override suspend fun isFollowee(userId: UserId, otherId: UserId): Boolean = transactionRunner {
    UserTable.innerJoin(FollowerTable, { id }, { FollowerTable.userId }, { FollowerTable.followeeId eq otherId.value })
      .slice(UserTable.id)
      .select { UserTable.id eq userId.value }
      .empty()
      .not()
  }
}
