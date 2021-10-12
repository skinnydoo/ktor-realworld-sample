package io.skinnydoo.users.usecases

import arrow.core.Either
import io.skinnydoo.common.NotFoundError
import io.skinnydoo.common.ResultUseCase
import io.skinnydoo.users.User
import io.skinnydoo.users.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import java.util.UUID

class GetUserWithId(
  dispatcher: CoroutineDispatcher,
  private val repository: UserRepository,
) : ResultUseCase<GetUserWithId.Params, Either<NotFoundError, User>>(dispatcher) {

  override suspend fun execute(params: Params): Either<NotFoundError, User> {
    return repository.userWithId(params.id)
  }

  data class Params(val id: UUID)
}
