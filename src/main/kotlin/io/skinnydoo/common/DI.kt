@file:Suppress("DuplicatedCode")

package io.skinnydoo.common

import io.skinnydoo.articles.ArticleRepository
import io.skinnydoo.articles.DefaultArticleRepository
import io.skinnydoo.articles.addArticleUseCaseFactory
import io.skinnydoo.articles.allArticlesUseCaseFactory
import io.skinnydoo.articles.deleteArticleUseCaseFactory
import io.skinnydoo.articles.getArticleWithSlugUseCaseFactory
import io.skinnydoo.articles.getFeedArticlesUseCaseFactory
import io.skinnydoo.articles.tags.DefaultTagRepository
import io.skinnydoo.articles.tags.TagRepository
import io.skinnydoo.articles.updateArticleUseCaseFactory
import io.skinnydoo.profiles.DefaultProfileRepository
import io.skinnydoo.profiles.ProfileRepository
import io.skinnydoo.profiles.followUserUseCaseFactory
import io.skinnydoo.profiles.getUserProfileUseCaseFactory
import io.skinnydoo.profiles.unfollowUserUseCaseFactory
import io.skinnydoo.users.AuthRepository
import io.skinnydoo.users.DefaultAuthRepository
import io.skinnydoo.users.DefaultUserRepository
import io.skinnydoo.users.UserRepository
import io.skinnydoo.users.getUserWithIdUseCaseFactory
import io.skinnydoo.users.loginUserUseCaseFactory
import io.skinnydoo.users.registerUserUseCaseFactory
import io.skinnydoo.users.updateUserUseCaseFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.single
import org.koin.logger.SLF4JLogger

fun KoinApplication.configure() {
  SLF4JLogger(level = Level.DEBUG)
  modules(koinModules())
}

fun koinModules(): List<Module> = listOf(appModule, repositoryModule, coroutinesModule, useCasesModule)

private val repositoryModule = module {
  single<DefaultUserRepository>() bind UserRepository::class
  single { DefaultAuthRepository(get(), get()) } bind AuthRepository::class
  single { DefaultProfileRepository(get()) } bind ProfileRepository::class
  single { DefaultArticleRepository(get(), get()) } bind ArticleRepository::class
  single { DefaultTagRepository() } bind TagRepository::class
}

private val coroutinesModule = module {
  single(IODispatcher) { Dispatchers.IO }
  single(DefaultDispatcher) { Dispatchers.Default }
  single<CoroutineDispatcher>(named("Main")) { Dispatchers.Main }
}

private val useCasesModule = module {
  single(named("register")) { registerUserUseCaseFactory(get(IODispatcher), get()) }
  single(named("login")) { loginUserUseCaseFactory(get(IODispatcher), get()) }
  single(named("updateUser")) { updateUserUseCaseFactory(get(IODispatcher), get()) }
  single(named("getUserWithId")) { getUserWithIdUseCaseFactory(get(IODispatcher), get()) }
  single(named("getUserProfile")) { getUserProfileUseCaseFactory(get(IODispatcher), get()) }
  single(named("followUser")) { followUserUseCaseFactory(get(IODispatcher), get()) }
  single(named("unfollowUser")) { unfollowUserUseCaseFactory(get(IODispatcher), get()) }
  single(named("addArticle")) { addArticleUseCaseFactory(get(IODispatcher), get()) }
  single(named("updateArticle")) { updateArticleUseCaseFactory(get(IODispatcher), get()) }
  single(named("getArticle")) { getArticleWithSlugUseCaseFactory(get(IODispatcher), get()) }
  single(named("allArticles")) { allArticlesUseCaseFactory(get(IODispatcher), get()) }
  single(named("deleteArticle")) { deleteArticleUseCaseFactory(get(IODispatcher), get()) }
  single(named("feed")) { getFeedArticlesUseCaseFactory(get(IODispatcher), get()) }
}

private val appModule = module {
  single { params -> DefaultDatabaseFactory(dbConfig = params.get()) } bind DatabaseFactory::class
  single { params -> JwtService(jwtConfig = params.get()) }

  single {
    Json {
      encodeDefaults = true
      isLenient = true
      allowSpecialFloatingPointValues = true
      allowStructuredMapKeys = true
      prettyPrint = false
      useArrayPolymorphism = true
      coerceInputValues = true
    }
  }
}
