package io.skinnydoo.articles.tags

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.skinnydoo.API_V1
import io.skinnydoo.common.handleErrors
import io.skinnydoo.common.models.TagsResponse
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

@Location("/tags")
class TagsRoute

fun Route.getTags() {
  val allTags by inject<GetTags>(named("tags"))

  get<TagsRoute> {
    allTags().map(::TagsResponse).fold({ handleErrors(it) }, { call.respond(it) })
  }
}

fun Application.registerTagsRoutes() {
  routing {
    route(API_V1) {
      getTags()
    }
  }
}
