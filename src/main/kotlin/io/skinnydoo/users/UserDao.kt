package io.skinnydoo.users

import io.skinnydoo.common.DatabaseTransactionRunner
import io.skinnydoo.common.Email
import io.skinnydoo.common.Password
import io.skinnydoo.common.UserId
import io.skinnydoo.common.Username
import io.skinnydoo.common.isNotEmpty
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface UserDao {
  suspend fun createFor(username: Username, email: Email, password: Password): UserId
  suspend fun userWithId(userId: UserId): User
  suspend fun userWithIdOrNull(userId: UserId): User?
  suspend fun userWithUsername(username: Username): User?
  suspend fun userWithEmail(email: Email): User?
  suspend fun userExist(username: Username, email: Email): Boolean
  suspend fun updateUserWithId(userId: UserId, details: UserUpdateDetails): Int
}

class DefaultUserDao(private val transactionRunner: DatabaseTransactionRunner) : UserDao {

  override suspend fun createFor(username: Username, email: Email, password: Password): UserId = transactionRunner {
    UserTable.insertAndGetId {
      it[UserTable.username] = username.value
      it[UserTable.email] = email.value
      it[UserTable.password] = password.value
    }.let { UserId(it.value) }
  }

  override suspend fun userWithId(userId: UserId): User = transactionRunner {
    UserTable.select { UserTable.id eq userId.value }.map(User.Companion::fromRow).single()
  }

  override suspend fun userWithIdOrNull(userId: UserId): User? = transactionRunner {
    UserTable.select { UserTable.id eq userId.value }.map(User.Companion::fromRow).singleOrNull()
  }

  override suspend fun userWithUsername(username: Username): User? = transactionRunner {
    UserTable.select { UserTable.username eq username.value }.map(User.Companion::fromRow).singleOrNull()
  }

  override suspend fun userWithEmail(email: Email): User? = transactionRunner {
    UserTable.select { UserTable.email eq email.value }.map(User.Companion::fromRow).singleOrNull()
  }

  override suspend fun userExist(username: Username, email: Email): Boolean = transactionRunner {
    UserTable.select { UserTable.username eq username.value or (UserTable.email eq email.value) }.isNotEmpty()
  }

  override suspend fun updateUserWithId(userId: UserId, details: UserUpdateDetails) = transactionRunner {
    UserTable.update({ UserTable.id eq userId.value }) { row ->
      if (details.email != null) row[email] = details.email.value
      if (details.bio != null) row[bio] = details.bio
      if (details.image != null) row[image] = details.image
    }
  }
}
