package io.skinnydoo.users.auth

import arrow.core.Either
import io.skinnydoo.common.LoginError
import io.skinnydoo.common.ResultUseCase
import io.skinnydoo.users.User
import io.skinnydoo.users.UserLoginCredentials
import kotlinx.coroutines.CoroutineDispatcher

class LoginUser(
  dispatcher: CoroutineDispatcher,
  private val repository: AuthRepository,
) : ResultUseCase<LoginUser.Params, Either<LoginError, User>>(dispatcher) {

  override suspend fun execute(params: Params): Either<LoginError, User> {
    val (email, password) = params.login
    return repository.login(email, password)
  }

  data class Params(val login: UserLoginCredentials)
}
