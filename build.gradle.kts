plugins {
  application
  kotlin("jvm") version Versions.kotlin
  kotlin("plugin.serialization") version Versions.kotlin
}

group = "io.skinnydoo"
version = "0.0.1"
application {
  mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(Deps.Koin.koin)
  implementation(Deps.Koin.koinLogger)
  
  implementation(Deps.Ktor.auth)
  implementation(Deps.Ktor.core)
  implementation(Deps.Ktor.hostCommon)
  implementation(Deps.Ktor.jwt)
  implementation(Deps.Ktor.locations)
  implementation(Deps.Ktor.netty)
  implementation(Deps.Ktor.session)
  implementation(Deps.Ktor.serialization)
  
  implementation(Deps.Ktor.Client.core)
  implementation(Deps.Ktor.Client.cio)
  implementation(Deps.Ktor.Client.serialization)
  implementation(Deps.Ktor.Client.auth)
  
  implementation(Deps.Exposed.core)
  implementation(Deps.Exposed.dao)
  implementation(Deps.Exposed.javaTime)
  implementation(Deps.Exposed.jdbc)
  
  implementation(Deps.jbcrypt)
  implementation(Deps.hikaricp)
  implementation(Deps.logback)
  implementation(Deps.mysqlConnector)
  
  implementation(Deps.logback)
  
  testImplementation(Deps.Testing.serverTest)
  testImplementation(Deps.Testing.ktTest)
}