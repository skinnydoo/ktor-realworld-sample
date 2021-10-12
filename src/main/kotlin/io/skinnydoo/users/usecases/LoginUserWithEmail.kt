package io.skinnydoo.users.usecases

import arrow.core.Either
import io.skinnydoo.common.LoginError
import io.skinnydoo.common.ResultUseCase
import io.skinnydoo.users.User
import io.skinnydoo.users.UserLoginWithEmail
import io.skinnydoo.users.auth.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher

class LoginUserWithEmail(
  dispatcher: CoroutineDispatcher,
  private val repository: AuthRepository,
) : ResultUseCase<LoginUserWithEmail.Params, Either<LoginError, User>>(dispatcher) {

  override suspend fun execute(params: Params): Either<LoginError, User> {
    val (email, password) = params.login
    return repository.login(email, password)
  }

  data class Params(val login: UserLoginWithEmail)
}
