package io.skinnydoo.users

import arrow.core.Either
import io.skinnydoo.common.models.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias LoginUser = suspend (UserLoginCredentials) -> Either<LoginErrors, User>
typealias RegisterUser = suspend (NewUserCredentials) -> Either<RegistrationErrors, User>

fun registerUserUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: AuthRepository,
): RegisterUser = { (username, email, password) ->
  withContext(dispatcher) { repository.register(username, email, password) }
}

fun loginUserUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: AuthRepository,
): LoginUser = { (email, password) -> withContext(dispatcher) { repository.login(email, password) } }
