package io.skinnydoo.users

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.skinnydoo.common.Email
import io.skinnydoo.common.Password
import io.skinnydoo.common.Username
import io.skinnydoo.common.checkPassword
import io.skinnydoo.common.models.LoginErrors
import io.skinnydoo.common.models.RegistrationErrors
import io.skinnydoo.common.models.User
import io.skinnydoo.common.models.UserAlreadyExist

interface AuthRepository {
  suspend fun register(username: Username, email: Email, password: Password): Either<RegistrationErrors, User>
  suspend fun login(email: Email, password: Password): Either<LoginErrors, User>
}

class DefaultAuthRepository(
  private val userDao: UserDao,
  private val userFollowerDao: UserFollowerDao,
) : AuthRepository {

  override suspend fun register(
    username: Username,
    email: Email,
    password: Password,
  ): Either<RegistrationErrors, User> = if (userDao.userExist(username, email)) {
    UserAlreadyExist("User exists").left()
  } else {
    val id = userDao.createFor(username, email, password)
    userFollowerDao.insert(id, id) // self follower
    userDao.userWithId(id).right()
  }

  override suspend fun login(
    email: Email,
    password: Password,
  ): Either<LoginErrors, User> = userDao.userWithEmail(email)?.let { user ->
    if (checkPassword(password.value, user.password)) {
      user.right()
    } else {
      LoginErrors.PasswordInvalid("Invalid password").left()
    }
  } ?: LoginErrors.EmailUnknown("Unknown email.").left()
}
