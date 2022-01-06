package io.skinnydoo.common.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.skinnydoo.common.logging.KotlinLoggingSqlLogger
import org.jetbrains.exposed.sql.Database
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.exposed.sql.DatabaseConfig as ExposedDatabaseConfig

class FakeDatabaseFactory(private val databaseConfig: DatabaseConfig) : DatabaseFactory {

  override fun connect() {
    Database.connect(hikari(), databaseConfig = config())
    SchemaDefinition.createSchema()
  }

  override fun close() {
    // no-app
  }

  private fun config(): ExposedDatabaseConfig = ExposedDatabaseConfig {
    sqlLogger = KotlinLoggingSqlLogger
    useNestedTransactions = true
    warnLongQueriesDuration = 50.milliseconds.inWholeMilliseconds
  }

  private fun hikari(): HikariDataSource {
    val config = HikariConfig().apply {
      isAutoCommit = true
      jdbcUrl = databaseConfig.url
      driverClassName = databaseConfig.driver
      maximumPoolSize = databaseConfig.maxPoolSize
      validate()
    }
    return HikariDataSource(config)
  }
}
