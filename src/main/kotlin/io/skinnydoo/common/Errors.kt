package io.skinnydoo.common

import io.skinnydoo.common.models.LoginResult
import io.skinnydoo.common.models.RegisterResult

typealias UserNotFound = UserErrors.UserNotFound

typealias ArticleNotFound = ArticleErrors.ArticleNotFound
typealias AuthorNotFound = ArticleErrors.AuthorNotFound
typealias CommentNotFound = ArticleErrors.CommentNotFound

typealias InvalidSlug = InvalidPropertyError.SlugInvalid

interface Error {
  val message: String
}

sealed interface CommonErrors : ArticleErrors
data class ServerError(val message: String = "Something went wrong") : CommonErrors
object Forbidden : CommonErrors

sealed interface LoginErrors : Error, LoginResult {
  data class EmailUnknown(override val message: String) : LoginErrors
  data class PasswordInvalid(override val message: String) : LoginErrors
}

sealed interface RegistrationErrors : Error, RegisterResult {
  data class UserAlreadyExist(override val message: String) : RegistrationErrors
}

sealed interface UserErrors {
  data class UserNotFound(val message: String = "User not found") : UserErrors
}

sealed interface ArticleErrors {
  data class ArticleNotFound(val slug: Slug) : ArticleErrors
  object AuthorNotFound : ArticleErrors
  data class CommentNotFound(val commentId: CommentId) : ArticleErrors
}

sealed class InvalidPropertyError {
  data class SlugInvalid(val message: String = "Invalid slug") : InvalidPropertyError()
}
