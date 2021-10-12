package io.skinnydoo.common.config

import io.skinnydoo.common.koinModules
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.logger.SLF4JLogger

fun KoinApplication.configure() {
  SLF4JLogger(level = Level.DEBUG)
  modules(koinModules())
}
