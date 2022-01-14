package io.skinnydoo.common.models

import io.skinnydoo.common.CommentId
import io.skinnydoo.common.Slug

typealias ArticleNotFound = ArticleErrors.ArticleNotFound
typealias CommentNotFound = ArticleErrors.CommentNotFound

typealias InvalidSlug = InvalidPropertyError.SlugInvalid

interface Error {
  val message: String
}

sealed interface AuthenticationErrors : Error, SelfQueryResult, UpdateSelfResult, ProfileMutationResult
data class Unauthorized(override val message: String) : AuthenticationErrors
data class TokenRequired(override val message: String) : AuthenticationErrors
data class TokenExpired(override val message: String) : AuthenticationErrors
data class TokenInvalid(override val message: String) : AuthenticationErrors

data class UserNotFound(override val message: String = "User not found") : Error, SelfQueryResult, UpdateSelfResult,
  ProfileResult, ProfileMutationResult

sealed interface CommonErrors : ArticleErrors
data class ServerError(val message: String = "Something went wrong") : CommonErrors
object Forbidden : CommonErrors

sealed interface LoginErrors : Error, LoginResult {
  data class EmailUnknown(override val message: String) : LoginErrors
  data class PasswordInvalid(override val message: String) : LoginErrors
}

sealed interface RegistrationErrors : Error, RegisterResult
data class UserAlreadyExist(override val message: String) : RegistrationErrors

sealed interface ArticleErrors {
  data class ArticleNotFound(val slug: Slug) : ArticleErrors
  object AuthorNotFound : ArticleErrors
  data class CommentNotFound(val commentId: CommentId) : ArticleErrors
}

sealed class InvalidPropertyError {
  data class SlugInvalid(val message: String = "Invalid slug") : InvalidPropertyError()
}
