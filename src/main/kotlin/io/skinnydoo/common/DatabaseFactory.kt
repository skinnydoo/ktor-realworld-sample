package io.skinnydoo.common

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.ApplicationEnvironment
import io.skinnydoo.articles.ArticleTable
import io.skinnydoo.articles.ArticleTagTable
import io.skinnydoo.articles.FavoriteArticleTable
import io.skinnydoo.articles.comments.CommentTable
import io.skinnydoo.articles.tags.TagTable
import io.skinnydoo.users.FollowerTable
import io.skinnydoo.users.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

interface DatabaseFactory {
  fun init(drop: Boolean = false)

  suspend fun <T> query(block: suspend Transaction.() -> T): T = newSuspendedTransaction { block() }
  suspend fun drop()
}

data class DBConfig(
  val driver: String,
  val url: String,
  val username: String,
  val password: String,
)

class DefaultDatabaseFactory(private val dbConfig: DBConfig) : DatabaseFactory {

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
    Database.connect(hikari()).apply {
      useNestedTransactions = true
    }

    transaction {
      addLogger(StdOutSqlLogger)
      if (drop) {
        transaction { SchemaUtils.drop(*tables) }
      }
      SchemaUtils.create(*tables)
    }
  }

  override suspend fun drop() {
    query { SchemaUtils.drop(*tables) }
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
