package io.skinnydoo.articles

import io.skinnydoo.common.DatabaseTransactionRunner
import io.skinnydoo.common.Slug
import io.skinnydoo.common.UserId
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

interface FavoriteArticleDao {
  suspend fun insert(slug: Slug, userId: UserId)
  suspend fun delete(slug: Slug)
}

class DefaultFavoriteArticleDao(private val transactionRunner: DatabaseTransactionRunner) : FavoriteArticleDao {

  override suspend fun insert(slug: Slug, userId: UserId): Unit = transactionRunner {
    FavoriteArticleTable.insert {
      it[articleSlug] = slug.value
      it[FavoriteArticleTable.userId] = userId.value
    }
  }

  override suspend fun delete(slug: Slug): Unit = transactionRunner {
    FavoriteArticleTable.deleteWhere { FavoriteArticleTable.articleSlug eq slug.value }
  }
}
