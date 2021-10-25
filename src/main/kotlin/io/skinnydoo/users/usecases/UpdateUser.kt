package io.skinnydoo.users.usecases

import arrow.core.Either
import io.skinnydoo.common.ResultUseCase
import io.skinnydoo.common.UserErrors
import io.skinnydoo.users.User
import io.skinnydoo.users.UserRepository
import io.skinnydoo.users.UserUpdateDetails
import kotlinx.coroutines.CoroutineDispatcher
import java.util.UUID

class UpdateUser(
  dispatcher: CoroutineDispatcher,
  private val repository: UserRepository,
) : ResultUseCase<UpdateUser.Params, Either<UserErrors, User>>(dispatcher) {

  override suspend fun execute(params: Params): Either<UserErrors, User> {
    return repository.updateUserDetails(params.userId, params.details)
  }

  data class Params(val userId: UUID, val details: UserUpdateDetails)
}
