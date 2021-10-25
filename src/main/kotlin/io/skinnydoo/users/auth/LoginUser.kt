package io.skinnydoo.users.auth

import arrow.core.Either
import io.skinnydoo.common.LoginErrors
import io.skinnydoo.common.ResultUseCase
import io.skinnydoo.users.User
import io.skinnydoo.users.UserLoginCredentials
import kotlinx.coroutines.CoroutineDispatcher

class LoginUser(
  dispatcher: CoroutineDispatcher,
  private val repository: AuthRepository,
) : ResultUseCase<LoginUser.Params, Either<LoginErrors, User>>(dispatcher) {

  override suspend fun execute(params: Params): Either<LoginErrors, User> {
    val (email, password) = params.login
    return repository.login(email, password)
  }

  data class Params(val login: UserLoginCredentials)
}
