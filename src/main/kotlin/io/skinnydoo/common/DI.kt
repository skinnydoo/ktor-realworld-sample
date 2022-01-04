@file:Suppress("DuplicatedCode")

package io.skinnydoo.common

import io.skinnydoo.articles.*
import io.skinnydoo.articles.comments.*
import io.skinnydoo.articles.tags.*
import io.skinnydoo.profiles.*
import io.skinnydoo.users.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.logger.SLF4JLogger

fun KoinApplication.configure() {
  SLF4JLogger(level = Level.DEBUG)
  modules(koinModules())
}

fun koinModules(): List<Module> = listOf(appModule, databaseModule, repositoryModule, coroutinesModule, useCasesModule)

private val repositoryModule = module {
  single { DefaultUserRepository(get()) } bind UserRepository::class
  single { DefaultAuthRepository(get(), get()) } bind AuthRepository::class
  single { DefaultProfileRepository(get(), get()) } bind ProfileRepository::class
  single { DefaultArticleRepository(get(), get(), get(), get()) } bind ArticleRepository::class
  single { DefaultTagRepository(get()) } bind TagRepository::class
  single { DefaultCommentRepository(get(), get()) } bind CommentRepository::class
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
  single(named("getArticle")) { getArticleWithSlugUseCaseFactory(get(IODispatcher), get()) }
  single(named("allArticles")) { allArticlesUseCaseFactory(get(IODispatcher), get()) }
  single(named("feed")) { getFeedArticlesUseCaseFactory(get(IODispatcher), get()) }
  single(named("updateArticle")) { updateArticleUseCaseFactory(get(IODispatcher), get()) }
  single(named("deleteArticle")) { deleteArticleUseCaseFactory(get(IODispatcher), get()) }
  single(named("favorArticle")) { favorArticleUseCaseFactory(get(IODispatcher), get()) }
  single(named("unFavorArticle")) { unFavorArticleUseCaseFactory(get(IODispatcher), get()) }

  single(named("commentsForArticle")) { getCommentsForArticleUseCaseFactory(get(IODispatcher), get()) }
  single(named("addComments")) { addCommentsForArticleUseCaseFactory(get(IODispatcher), get()) }
  single(named("removeComments")) { removeCommentFromArticleUseCaseFactory(get(IODispatcher), get()) }

  single(named("tags")) { getAllTagsUseCaseFactory(get(IODispatcher), get()) }
}

private val appModule = module {
  single { params -> DefaultDatabaseFactory(dbConfig = params.get(), get()) } bind DatabaseFactory::class
  single { params -> JwtService(jwtConfig = params.get()) }

  single {
    Json {
      encodeDefaults = true
      isLenient = true
      prettyPrint = false
      coerceInputValues = true
    }
  }
}

private val databaseModule = module {
  single { ExposedTransactionRunner(get(IODispatcher)) } bind DatabaseTransactionRunner::class
  single { DefaultUserDao(get()) } bind UserDao::class
  single { DefaultUserFollowerDao(get()) } bind UserFollowerDao::class
  single { DefaultArticleDao(get(), get(), get()) } bind ArticleDao::class
  single { DefaultFavoriteArticleDao(get()) } bind FavoriteArticleDao::class
  single { DefaultArticleTagDao(get()) } bind ArticleTagDao::class
  single { DefaultTagDao(get()) } bind TagDao::class
  single { DefaultCommentsDao(get(), get()) } bind CommentsDao::class
}
