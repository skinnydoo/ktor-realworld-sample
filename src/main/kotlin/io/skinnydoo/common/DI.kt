@file:Suppress("DuplicatedCode")

package io.skinnydoo.common

import io.skinnydoo.articles.*
import io.skinnydoo.articles.comments.*
import io.skinnydoo.articles.tags.*
import io.skinnydoo.common.db.DatabaseFactory
import io.skinnydoo.common.db.DatabaseTransactionRunner
import io.skinnydoo.common.db.DefaultDatabaseFactory
import io.skinnydoo.common.db.ExposedTransactionRunner
import io.skinnydoo.profiles.*
import io.skinnydoo.users.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

fun KoinApplication.configure(koinModules: List<Module>) {
  // logger(KotlinLoggingKoinLogger(Level.DEBUG))
  allowOverride(true)
  modules(koinModules)
}

fun koinModules(): List<Module> = listOf(appModule, databaseModule, repositoryModule, coroutinesModule, useCasesModule)

private val repositoryModule = module {
  single<UserRepository> { DefaultUserRepository(get()) }
  single<AuthRepository> { DefaultAuthRepository(get(), get()) }
  single<ProfileRepository> { DefaultProfileRepository(get(), get()) }
  single<ArticleRepository> { DefaultArticleRepository(get(), get(), get(), get()) }
  single<TagRepository> { DefaultTagRepository(get()) }
  single<CommentRepository> { DefaultCommentRepository(get(), get()) }
}

private val coroutinesModule = module {
  single(IODispatcher) { Dispatchers.IO }
  single(DefaultDispatcher) { Dispatchers.Default }
  single<CoroutineDispatcher>(named("Main")) { Dispatchers.Main }
}

private val useCasesModule = module {
  factory(named("register")) { registerUserUseCaseFactory(get(IODispatcher), get()) }
  factory(named("login")) { loginUserUseCaseFactory(get(IODispatcher), get()) }
  factory(named("updateUser")) { updateUserUseCaseFactory(get(IODispatcher), get()) }
  factory(named("getUserWithId")) { getUserWithIdUseCaseFactory(get(IODispatcher), get()) }
  factory(named("getUserProfile")) { getUserProfileUseCaseFactory(get(IODispatcher), get()) }
  factory(named("followUser")) { followUserUseCaseFactory(get(IODispatcher), get()) }
  factory(named("unfollowUser")) { unfollowUserUseCaseFactory(get(IODispatcher), get()) }

  factory(named("addArticle")) { addArticleUseCaseFactory(get(IODispatcher), get()) }
  factory(named("getArticle")) { getArticleWithSlugUseCaseFactory(get(IODispatcher), get()) }
  factory(named("allArticles")) { allArticlesUseCaseFactory(get(IODispatcher), get()) }
  factory(named("feed")) { getFeedArticlesUseCaseFactory(get(IODispatcher), get()) }
  factory(named("updateArticle")) { updateArticleUseCaseFactory(get(IODispatcher), get()) }
  factory(named("deleteArticle")) { deleteArticleUseCaseFactory(get(IODispatcher), get()) }
  factory(named("favorArticle")) { favorArticleUseCaseFactory(get(IODispatcher), get()) }
  factory(named("unFavorArticle")) { unFavorArticleUseCaseFactory(get(IODispatcher), get()) }

  factory(named("commentsForArticle")) { getCommentsForArticleUseCaseFactory(get(IODispatcher), get()) }
  factory(named("addComments")) { addCommentsForArticleUseCaseFactory(get(IODispatcher), get()) }
  factory(named("removeComments")) { removeCommentFromArticleUseCaseFactory(get(IODispatcher), get()) }

  factory(named("tags")) { getAllTagsUseCaseFactory(get(IODispatcher), get()) }
}

private val appModule = module {
  single<DatabaseFactory> { params -> DefaultDatabaseFactory(databaseConfig = params.get()) }
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
  single<DatabaseTransactionRunner> { ExposedTransactionRunner(get(IODispatcher)) }
  single<UserDao> { DefaultUserDao(get()) }
  single<UserFollowerDao> { DefaultUserFollowerDao(get()) }
  single<ArticleDao> { DefaultArticleDao(get(), get(), get()) }
  single<FavoriteArticleDao> { DefaultFavoriteArticleDao(get()) }
  single<ArticleTagDao> { DefaultArticleTagDao(get()) }
  single<TagDao> { DefaultTagDao(get()) } bind TagDao::class
  single<CommentsDao> { DefaultCommentsDao(get(), get()) }
}
