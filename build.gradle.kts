import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets

plugins {
  application
  kotlin("jvm") version Versions.kotlin
  kotlin("plugin.serialization") version Versions.kotlin
  id("com.diffplug.spotless") version Versions.spotless
  id("org.unbroken-dome.test-sets") version "4.0.0"
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

  implementation(Deps.coroutines)
  implementation(Deps.coroutinesSlf4j)

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
  implementation(Deps.serializationJson)

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
  implementation(Deps.kotlinLogging)
  implementation(Deps.mysqlConnector)

  implementation(Deps.scientist)

  implementation("commons-validator:commons-validator:1.7")
  implementation("com.auth0:java-jwt:3.18.2")

  testImplementation(Deps.Testing.serverTest)
  testImplementation(Deps.Testing.coroutinesTest)
  testImplementation(Deps.Testing.kotestJunit5)
  testImplementation(Deps.Testing.kotestAssertionsCore)
  testImplementation(Deps.Testing.kotestAssertionsJson)
  testImplementation(Deps.Testing.kotestAssertionsKtor)
  testImplementation(Deps.Testing.kotestAssertionsArrow)
  testImplementation(Deps.Testing.h2)
}

spotless {
  kotlin {
    ktlint(Versions.ktlint).userData(
      mapOf(
        "indent_size" to "2",
        "indent_style" to "space",
        "tab_width" to "2",
        "max_line_length" to "120",
        "disabled_rules" to "no-wildcard-imports",
      )
    )
  }
  kotlinGradle {
    target("*.gradle.kts")
    ktlint(Versions.ktlint)
  }
}

testSets {
  val integrationTest by creating
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    languageVersion = "1.6"
    freeCompilerArgs = freeCompilerArgs + listOf(
      "-Xopt-in=kotlin.RequiresOptIn",
      "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
      "-Xopt-in=io.ktor.locations.KtorExperimentalLocationsAPI",
      "-Xopt-in=kotlin.time.ExperimentalTime",
      "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    )
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
