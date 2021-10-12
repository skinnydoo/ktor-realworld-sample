import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm") version Versions.kotlin
  kotlin("plugin.serialization") version Versions.kotlin
  id("com.diffplug.spotless") version Versions.spotless
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
  implementation(platform(Deps.Arrow.stack))

  implementation(Deps.Arrow.core)

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

  implementation("commons-validator:commons-validator:1.7")

  testImplementation(Deps.Testing.serverTest)
  testImplementation(Deps.Testing.ktTest)
}

spotless {
  kotlin {
    ktlint(Versions.ktlint).userData(
      mapOf(
        "indent_size" to "2",
        "indent_style" to "space",
        "tab_width" to "2",
        "max_line_length" to "120",
      )
    )
  }
  kotlinGradle {
    target("*.gradle.kts")
    ktlint(Versions.ktlint)
  }
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + listOf(
      "-Xopt-in=kotlin.RequiresOptIn",
    )
  }
}
