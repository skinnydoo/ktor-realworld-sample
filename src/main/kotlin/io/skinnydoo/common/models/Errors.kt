package io.skinnydoo.common.models

typealias InvalidSlug = InvalidPropertyError.SlugInvalid

interface Error {
  val message: String
}

sealed interface AuthenticationErrors : Error, SelfQueryResult, UpdateSelfResult, ProfileMutationResult,
  ArticleFeedResult, CreateArticleResult, ArticleMutationResult, DeleteArticleResult

data class Unauthorized(override val message: String) : AuthenticationErrors
data class TokenRequired(override val message: String) : AuthenticationErrors
data class TokenExpired(override val message: String) : AuthenticationErrors
data class TokenInvalid(override val message: String) : AuthenticationErrors

data class UserNotFound(override val message: String = "User not found") : Error, SelfQueryResult, UpdateSelfResult,
  ProfileResult, ProfileMutationResult

sealed interface AuthorizationErrors : Error, ArticleErrors, ArticleMutationResult, DeleteArticleResult
data class Forbidden(override val message: String) : AuthorizationErrors

data class ServerError(override val message: String = "Something went wrong") : Error, ArticleErrors, ArticleListResult,
  ArticleFeedResult, CreateArticleResult, ArticleMutationResult, DeleteArticleResult

sealed interface LoginErrors : Error, LoginResult {
  data class EmailUnknown(override val message: String) : LoginErrors
  data class PasswordInvalid(override val message: String) : LoginErrors
}

sealed interface RegistrationErrors : Error, RegisterResult
data class UserAlreadyExist(override val message: String) : RegistrationErrors

sealed interface ArticleErrors : Error
data class ArticleNotFound(override val message: String) : ArticleErrors, ArticleResult, ArticleMutationResult,
  DeleteArticleResult

data class CommentNotFound(override val message: String) : Error, ArticleErrors

sealed class InvalidPropertyError : Error {
  data class SlugInvalid(override val message: String = "Invalid slug") : InvalidPropertyError(), ArticleResult,
    ArticleMutationResult, DeleteArticleResult
}
