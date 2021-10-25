package io.skinnydoo.common.config

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import io.skinnydoo.common.ArticleErrors
import io.skinnydoo.common.CommonErrors
import io.skinnydoo.common.ErrorEnvelope
import io.skinnydoo.common.Forbidden
import io.skinnydoo.common.InvalidPropertyError
import io.skinnydoo.common.ServerError
import io.skinnydoo.common.UserErrors

fun StatusPages.Configuration.configure() {
  exception<Throwable> { e ->
    call.respond(HttpStatusCode.InternalServerError, ErrorEnvelope(mapOf("body" to listOf(e.localizedMessage))))
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: UserErrors) = when (error) {
  is UserErrors.UserAlreadyExist -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(HttpStatusCode.UnprocessableEntity, errorBody)
  }
  is UserErrors.UserNotFound -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(HttpStatusCode.UnprocessableEntity, errorBody)
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: ArticleErrors) = when (error) {
  is ArticleErrors.ArticleNotFound -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf("Article with slug ${error.slug} does not exist")))
    call.respond(HttpStatusCode.UnprocessableEntity, errorBody)
  }
  ArticleErrors.AuthorNotFound -> call.respond(HttpStatusCode.InternalServerError)
  Forbidden -> call.respond(HttpStatusCode.Unauthorized)
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: InvalidPropertyError) = when (error) {
  is InvalidPropertyError.SlugInvalid -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(HttpStatusCode.UnprocessableEntity, errorBody)
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: CommonErrors) = when (error) {
  is ServerError -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(HttpStatusCode.InternalServerError, errorBody)
  }
  Forbidden -> call.respond(HttpStatusCode.Unauthorized)
}
