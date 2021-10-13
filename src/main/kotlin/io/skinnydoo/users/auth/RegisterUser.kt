package io.skinnydoo.users.auth

import arrow.core.Either
import io.skinnydoo.common.AlreadyExistsError
import io.skinnydoo.common.ResultUseCase
import io.skinnydoo.users.NewUserCredentials
import io.skinnydoo.users.User
import kotlinx.coroutines.CoroutineDispatcher

class RegisterUser(
  dispatcher: CoroutineDispatcher,
  private val repository: AuthRepository,
) : ResultUseCase<RegisterUser.Params, Either<AlreadyExistsError, User>>(dispatcher) {
  override suspend fun execute(params: Params): Either<AlreadyExistsError, User> {
    val (username, email, password) = params.credentials
    return repository.register(username, email, password)
  }

  data class Params(val credentials: NewUserCredentials)
}
