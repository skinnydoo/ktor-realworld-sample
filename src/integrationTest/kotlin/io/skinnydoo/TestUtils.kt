package io.skinnydoo

import io.ktor.config.*
import io.ktor.server.testing.*
import io.skinnydoo.common.db.DatabaseFactory
import io.skinnydoo.common.db.FakeDatabaseFactory
import io.skinnydoo.common.koinModules
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

fun MapApplicationConfig.setupTestConfig() {
  // JWT config
  put("ktor.jwt.secret", "lLvnBDlbtKvTdpVeYWGKkRxtBH2rlV3uzKKWWvvOnqQ=")
  put("ktor.jwt.validity_ms", "300000")
  put("ktor.jwt.issuer", "io.skinnydoo")
  put("ktor.jwt.audience", "realworld")
  put("ktor.jwt.realm", "RealWorld Server")

  // storage config
  put("ktor.database.driver", "org.h2.Driver")
  put("ktor.database.url", "jdbc:h2:mem:realworld;DATABASE_TO_LOWER=TRUE;MODE=MYSQL")
  put("ktor.database.username", "sa")
  put("ktor.database.pwd", "password")
  put("ktor.database.maxPoolSize", "1")
}

fun koinTestModules() = koinModules() + appTestModule

val json = Json {
  encodeDefaults = true
  isLenient = true
  prettyPrint = false
  coerceInputValues = true
}

private val appTestModule = module {
  single<DatabaseFactory> { params -> FakeDatabaseFactory(databaseConfig = params.get()) }
}

fun withTestServer(koinModules: List<Module> = koinTestModules(), block: TestApplicationEngine.() -> Unit) {
  withTestApplication({
    (environment.config as MapApplicationConfig).apply { setupTestConfig() }
    module(koinModules)
  }, block)
}
