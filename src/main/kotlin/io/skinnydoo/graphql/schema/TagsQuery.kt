package io.skinnydoo.graphql.schema

import com.expediagroup.graphql.server.operations.Query
import io.skinnydoo.articles.tags.GetTags
import io.skinnydoo.common.models.Tag

class TagsQuery(private val getTagsUseCase: GetTags) : Query {

  suspend fun tags(): List<String> = getTagsUseCase().fold({ emptyList() }, { it.map(Tag::value) })
}
