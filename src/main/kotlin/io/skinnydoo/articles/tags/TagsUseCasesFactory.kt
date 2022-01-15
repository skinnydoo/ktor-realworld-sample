package io.skinnydoo.articles.tags

import arrow.core.Either
import io.skinnydoo.common.models.ServerError
import io.skinnydoo.common.models.Tag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias GetTags = suspend () -> Either<ServerError, List<Tag>>

fun getAllTagsUseCaseFactory(
  dispatcher: CoroutineDispatcher,
  repository: TagRepository,
): GetTags = { withContext(dispatcher) { repository.getTags() } }
