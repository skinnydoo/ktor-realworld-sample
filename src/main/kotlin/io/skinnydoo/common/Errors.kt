package io.skinnydoo.common

typealias UserNotFound = UserErrors.UserNotFound
typealias UserExists = UserErrors.UserAlreadyExist

typealias ArticleNotFound = ArticleErrors.ArticleNotFound
typealias AuthorNotFound = ArticleErrors.AuthorNotFound

typealias InvalidSlug = InvalidPropertyError.SlugInvalid

sealed interface CommonErrors
data class ServerError(val message: String = "Something went wrong") : CommonErrors
object Forbidden : CommonErrors, ArticleErrors

sealed interface LoginErrors {
  object EmailUnknown : LoginErrors
  object PasswordInvalid : LoginErrors
}

sealed interface UserErrors {
  data class UserAlreadyExist(val message: String = "User exists") : UserErrors
  data class UserNotFound(val message: String = "User not found") : UserErrors
}

sealed interface ArticleErrors {
  data class ArticleNotFound(val slug: Slug) : ArticleErrors
  object AuthorNotFound : ArticleErrors
}

sealed class InvalidPropertyError {
  data class SlugInvalid(val message: String = "Invalid slug") : InvalidPropertyError()
}
