package io.skinnydoo.users.usecases

import arrow.core.Either
import io.skinnydoo.common.LoginError
import io.skinnydoo.common.ResultUseCase
import io.skinnydoo.users.User
import io.skinnydoo.users.UserLoginWithUserName
import io.skinnydoo.users.auth.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher

class LoginUserWithUsername(
  dispatcher: CoroutineDispatcher,
  private val repository: AuthRepository,
) : ResultUseCase<LoginUserWithUsername.Params, Either<LoginError, User>>(dispatcher) {

  override suspend fun execute(params: Params): Either<LoginError, User> {
    val (username, password) = params.login
    return repository.login(username, password)
  }

  data class Params(val login: UserLoginWithUserName)
}
