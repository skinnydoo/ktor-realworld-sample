package io.skinnydoo.articles.tags

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.skinnydoo.API_V1
import io.skinnydoo.common.handleErrors
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
