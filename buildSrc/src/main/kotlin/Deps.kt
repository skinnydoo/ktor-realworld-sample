object Deps {

  const val logback = "ch.qos.logback:logback-classic:${Versions.logback}"
  const val mysqlConnector = "mysql:mysql-connector-java:${Versions.mysqlConnector}"
  const val hikaricp = "com.zaxxer:HikariCP:${Versions.hikaricp}"
  const val jbcrypt = "org.mindrot:jbcrypt:${Versions.jbcrypt}"

  object Arrow {
    const val stack = "io.arrow-kt:arrow-stack:${Versions.arrow}"
    const val core = "io.arrow-kt:arrow-core"
  }

  object Ktor {
    const val core = "io.ktor:ktor-server-core:${Versions.ktor}"
    const val auth = "io.ktor:ktor-auth:${Versions.ktor}"
    const val jwt = "io.ktor:ktor-auth-jwt:${Versions.ktor}"
    const val session = "io.ktor:ktor-server-sessions:${Versions.ktor}"
    const val locations = "io.ktor:ktor-locations:${Versions.ktor}"
    const val hostCommon = "io.ktor:ktor-server-host-common:${Versions.ktor}"
    const val serialization = "io.ktor:ktor-serialization:${Versions.ktor}"
    const val netty = "io.ktor:ktor-server-netty:${Versions.ktor}"

    object Client {
      const val core = "io.ktor:ktor-client-core:${Versions.ktor}"
      const val cio = "io.ktor:ktor-client-cio:${Versions.ktor}"
      const val serialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"
      const val auth = "io.ktor:ktor-client-auth:${Versions.ktor}"
    }
  }

  object Exposed {
    const val core = "org.jetbrains.exposed:exposed-core:${Versions.exposed}"
    const val dao = "org.jetbrains.exposed:exposed-dao:${Versions.exposed}"
    const val jdbc = "org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}"
    const val javaTime = "org.jetbrains.exposed:exposed-java-time:${Versions.exposed}"
  }

  object Koin {
    const val koin = "io.insert-koin:koin-ktor:${Versions.koin}"
    const val koinLogger = "io.insert-koin:koin-logger-slf4j:${Versions.koin}"
  }

  object Testing {
    const val serverTest = "io.ktor:ktor-server-tests:${Versions.ktor}"
    const val ktTest = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"
  }
}
