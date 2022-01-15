package io.skinnydoo.common

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.skinnydoo.common.models.*

fun StatusPages.Configuration.configure() {
  exception<Throwable> { e ->
    application.log.error(e)
    call.respond(HttpStatusCode.InternalServerError, ErrorEnvelope(mapOf("body" to listOf(e.localizedMessage))))
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: LoginErrors) = when (error) {
  is LoginErrors.EmailUnknown -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(status = HttpStatusCode.Unauthorized, message = errorBody)
  }
  is LoginErrors.PasswordInvalid -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(status = HttpStatusCode.Unauthorized, message = errorBody)
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: UserNotFound) {
  val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
  call.respond(HttpStatusCode.NotFound, errorBody)
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: RegistrationErrors) = when (error) {
  is UserAlreadyExist -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(HttpStatusCode.UnprocessableEntity, errorBody)
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: ArticleErrors) = when (error) {
  is ArticleErrors.ArticleNotFound -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf("Article with slug ${error.slug} does not exist")))
    call.respond(HttpStatusCode.NotFound, errorBody)
  }
  ArticleErrors.AuthorNotFound -> call.respond(HttpStatusCode.InternalServerError)
  is Forbidden -> call.respond(HttpStatusCode.Unauthorized)
  is ServerError -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(HttpStatusCode.InternalServerError, errorBody)
  }
  is ArticleErrors.CommentNotFound -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf("Comment with id ${error.commentId} does not exist")))
    call.respond(HttpStatusCode.NotFound, errorBody)
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: InvalidPropertyError) = when (error) {
  is InvalidPropertyError.SlugInvalid -> {
    val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
    call.respond(HttpStatusCode.UnprocessableEntity, errorBody)
  }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: ServerError) {
  val errorBody = ErrorEnvelope(mapOf("body" to listOf(error.message)))
  call.respond(HttpStatusCode.InternalServerError, errorBody)
}

suspend fun PipelineContext<Unit, ApplicationCall>.handleErrors(error: AuthorizationErrors) = when (error) {
  is Forbidden -> call.respond(HttpStatusCode.Unauthorized)
}
