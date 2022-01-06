package io.skinnydoo.common.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.skinnydoo.common.logging.KotlinLoggingSqlLogger
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.exposed.sql.DatabaseConfig as ExposedDatabaseConfig

private val logger = KotlinLogging.logger {}

interface DatabaseFactory {
  fun connect()
  fun close()
}

data class DatabaseConfig(
  val driver: String,
  val url: String,
  val username: String,
  val password: String,
  val maxPoolSize: Int,
)

class DefaultDatabaseFactory(private val databaseConfig: DatabaseConfig) : DatabaseFactory {

  override fun connect() {
    logger.info { "Initializing DB connection" }
    Database.connect(hikari(), databaseConfig = config())
    SchemaDefinition.createSchema()
    logger.info { "DB initialization complete" }
  }

  override fun close() {
    // no-op
  }

  private fun config(): ExposedDatabaseConfig = ExposedDatabaseConfig {
    sqlLogger = KotlinLoggingSqlLogger
    useNestedTransactions = true
    warnLongQueriesDuration = 50.milliseconds.inWholeMilliseconds
  }

  private fun hikari(): HikariDataSource {
    val config = HikariConfig().apply {
      isAutoCommit = false
      transactionIsolation = "TRANSACTION_REPEATABLE_READ"

      jdbcUrl = databaseConfig.url
      driverClassName = databaseConfig.driver
      username = databaseConfig.username
      password = databaseConfig.password
      maximumPoolSize = databaseConfig.maxPoolSize

      addDataSourceProperty("rewriteBatchedStatements", true)
    }.also { it.validate() }
    return HikariDataSource(config)
  }
}

fun ApplicationEnvironment.dbConfig(path: String): DatabaseConfig = with(config.config(path)) {
  DatabaseConfig(driver = property("driver").getString(),
    url = property("url").getString(),
    username = property("username").getString(),
    password = property("pwd").getString(),
    maxPoolSize = property("maxPoolSize").getString().toInt())
}
