package io.skinnydoo.common

import io.skinnydoo.users.DefaultUserRepository
import io.skinnydoo.users.UserRepository
import io.skinnydoo.users.auth.AuthRepository
import io.skinnydoo.users.auth.DefaultAuthRepository
import io.skinnydoo.users.usecases.GetUserWithId
import io.skinnydoo.users.usecases.LoginUserWithEmail
import io.skinnydoo.users.usecases.LoginUserWithUsername
import io.skinnydoo.users.usecases.RegisterUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.single

fun koinModules(): List<Module> = listOf(appModule, repositoryModule, coroutinesModule, useCasesModule)

private val repositoryModule = module {
  single<DefaultUserRepository>() bind UserRepository::class
  single { DefaultAuthRepository(get()) } bind AuthRepository::class
}

val coroutinesModule = module {
  single<CoroutineDispatcher>(named("IO")) { Dispatchers.IO }
  single<CoroutineDispatcher>(named("Default")) { Dispatchers.Default }
  single<CoroutineDispatcher>(named("Main")) { Dispatchers.Main }
}

private val useCasesModule = module {
  single { RegisterUser(get(named("IO")), get()) }
  single { LoginUserWithEmail(get(named("IO")), get()) }
  single { LoginUserWithUsername(get(named("IO")), get()) }
  single { GetUserWithId(get(named("IO")), get()) }
}

private val appModule = module {
  single { params -> DefaultDatabaseFactory(dbConfig = params.get()) } bind DatabaseFactory::class
  single { params -> JwtService(jwtConfig = params.get()) }

  single {
    Json {
      encodeDefaults = true
      isLenient = true
      ignoreUnknownKeys = true
      coerceInputValues = true
      allowSpecialFloatingPointValues = true
      allowStructuredMapKeys = true
      prettyPrint = false
      useArrayPolymorphism = true
    }
  }
}
