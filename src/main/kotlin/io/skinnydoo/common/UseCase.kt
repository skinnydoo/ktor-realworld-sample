package io.skinnydoo.common

import arrow.core.Either
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Executes business logic asynchronously.
 */
abstract class ResultUseCase<in P, R>(private val dispatcher: CoroutineDispatcher) {

  /**
   * Executes the use case asynchronously and returns an [Either].
   *
   * @return an [Either].
   *
   * @param params the input parameters to run the use case with
   */
  suspend operator fun invoke(params: P): Either<Throwable, R> = Either.catch {
    withContext(dispatcher) { execute(params) }
  }

  /**
   * Override this to set the code to be executed.
   * @param params the input parameters to run the use case with
   */
  protected abstract suspend fun execute(params: P): R
}
