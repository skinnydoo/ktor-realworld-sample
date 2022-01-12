package io.skinnydoo.graphql

import arrow.core.Either
import arrow.core.left
import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.ktor.http.auth.*
import io.skinnydoo.common.JwtService
import io.skinnydoo.common.UserId
import io.skinnydoo.common.models.AuthenticationErrors
import io.skinnydoo.common.models.User
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class AuthContext(val user: User) : GraphQLContext

/**
 * Takes an auth header and returns the userId
 */
fun getUserId(auth: HttpAuthHeader?, jwtService: JwtService): Either<AuthenticationErrors, UserId> {
  if (auth == null) return AuthenticationErrors.LoginRequired("You must be logged in.").left()

  val token = when {
    auth is HttpAuthHeader.Single && auth.authScheme == "Token" -> auth.blob
    else -> return AuthenticationErrors.TokenRequired("Token is required").left()
  }

  val jwt = runCatching {
    jwtService.verifier.verify(token)
  }.onFailure { logger.trace(it) { "Token verification failed" } }
    .getOrNull() ?: return AuthenticationErrors.InvalidToken("Invalid Token").left()

  return UserId.fromString(jwt.subject)
    .toEither { AuthenticationErrors.InvalidToken("Invalid Token") }
}
