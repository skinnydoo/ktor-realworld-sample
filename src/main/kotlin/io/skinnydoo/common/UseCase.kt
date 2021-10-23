package io.skinnydoo.common

import arrow.core.Either
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.Duration

/**
 * Executes business logic asynchronously.
 */
abstract class UseCase<in P>(private val dispatcher: CoroutineDispatcher) {

  /**
   * Executes the use case asynchronously.
   *
   * @param params the input parameters to run the use case with.
   * @param timeoutMs a timeout within which the use case must run.
   */
  suspend operator fun invoke(params: P, timeoutMs: Long = defaultTimeoutMs) {
    runCatching {
      withTimeout(timeoutMs) {
        withContext(dispatcher) { execute(params) }
      }
    }.onFailure { e ->
      if (e is CancellationException) {
        throw e // Do not suppress CancellationException. Allow parent coroutines to cancel
      }
    }
  }

  /**
   * Override this to set the code to be executed.
   * @param params the input parameters to run the use case with
   */
  protected abstract suspend fun execute(params: P)

  companion object {
    private val defaultTimeoutMs: Long = Duration.ofMillis(5).toMillis()
  }
}

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
