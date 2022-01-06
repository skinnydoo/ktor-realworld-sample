package io.skinnydoo.common.db

import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface DatabaseTransactionRunner {
  suspend operator fun <T> invoke(block: suspend Transaction.() -> T): T
}

class ExposedTransactionRunner(private val dispatcher: CoroutineDispatcher) : DatabaseTransactionRunner {
  override suspend fun <T> invoke(block: suspend Transaction.() -> T): T {
    return newSuspendedTransaction(dispatcher) { block() }
  }
}
