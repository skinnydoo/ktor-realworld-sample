package io.skinnydoo.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.ApplicationEnvironment
import io.skinnydoo.articles.ArticleTable
import io.skinnydoo.articles.ArticleTagTable
import io.skinnydoo.articles.FavoriteArticleTable
import io.skinnydoo.articles.comments.CommentTable
import io.skinnydoo.articles.tags.TagTable
import io.skinnydoo.common.logging.KotlinLoggingSqlLogger
import io.skinnydoo.users.FollowerTable
import io.skinnydoo.users.UserTable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

interface DatabaseFactory {
  fun init(drop: Boolean = false)
  suspend fun drop()
}

data class DBConfig(
  val driver: String,
  val url: String,
  val username: String,
  val password: String,
)

class DefaultDatabaseFactory(
  private val dbConfig: DBConfig,
  private val transactionRunner: DatabaseTransactionRunner,
) : DatabaseFactory {

  private val tables: Array<Table> = arrayOf(
    UserTable,
    FollowerTable,
    ArticleTable,
    ArticleTagTable,
    FavoriteArticleTable,
    CommentTable,
    TagTable,
  )

  override fun init(drop: Boolean) {
    logger.info { "Initializing DB connection" }

    val dbConfig = DatabaseConfig {
      sqlLogger = KotlinLoggingSqlLogger
      useNestedTransactions = true
      warnLongQueriesDuration = 50.milliseconds.inWholeMilliseconds
    }
    Database.connect(hikari(), databaseConfig = dbConfig)

    transaction {
      if (drop) {
        transaction { SchemaUtils.drop(*tables) }
      }
      SchemaUtils.create(*tables)
    }
    logger.info { "DB initialization complete" }
  }

  override suspend fun drop() {
    transactionRunner { SchemaUtils.drop(*tables) }
  }

  private fun hikari(): HikariDataSource {
    val config = HikariConfig().apply {
      maximumPoolSize = 3
      isAutoCommit = false
      transactionIsolation = "TRANSACTION_REPEATABLE_READ"

      jdbcUrl = dbConfig.url
      driverClassName = dbConfig.driver
      username = dbConfig.username
      password = dbConfig.password

      addDataSourceProperty("rewriteBatchedStatements", true)
    }.also { it.validate() }
    return HikariDataSource(config)
  }
}

fun ApplicationEnvironment.dbConfig(path: String): DBConfig = with(config.config(path)) {
  DBConfig(
    driver = property("driver").getString(),
    url = property("url").getString(),
    username = property("username").getString(),
    password = property("pwd").getString(),
  )
}
