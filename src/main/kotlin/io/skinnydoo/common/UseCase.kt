package io.skinnydoo.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.Duration

abstract class UseCase<in P>(private val dispatcher: CoroutineDispatcher) {

  suspend operator fun invoke(params: P, timeoutMs: Long = defaultTimeoutMs) {
    runCatching {
      withTimeout(defaultTimeoutMs) {
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
  @Throws(RuntimeException::class)
  protected abstract suspend fun execute(params: P)

  companion object {
    private val defaultTimeoutMs: Long = Duration.ofMillis(5).toMillis()
  }
}

abstract class ResultUseCase<in P, R>(private val dispatcher: CoroutineDispatcher) {

  suspend operator fun invoke(params: P): Result<R> = runCatching {
    withContext(dispatcher) { execute(params) }
  }.onFailure { e ->
    if (e is CancellationException) {
      throw e // Do not suppress CancellationException. Allow parent coroutines to cancel
    }
  }

  /**
   * Override this to set the code to be executed.
   * @param params the input parameters to run the use case with
   */
  @Throws(RuntimeException::class)
  protected abstract suspend fun execute(params: P): R
}
