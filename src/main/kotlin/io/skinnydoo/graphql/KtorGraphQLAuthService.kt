package io.skinnydoo.graphql

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import com.auth0.jwt.exceptions.TokenExpiredException
import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.ktor.http.auth.*
import io.skinnydoo.common.JwtService
import io.skinnydoo.common.UserId
import io.skinnydoo.common.models.*
import io.skinnydoo.users.GetUserWithId
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class KtorGraphQLAuthService(private val jwtService: JwtService, private val userWithId: GetUserWithId) {

  /**
   * Takes an auth header and returns the user
   */
  suspend fun getUser(auth: HttpAuthHeader?): Either<AuthenticationErrors, User> {
    if (auth == null) return TokenRequired("You must be logged in.").left()

    val token = when {
      auth is HttpAuthHeader.Single && auth.authScheme == "Token" -> auth.blob
      else -> return TokenRequired("Invalid auth header").left()
    }

    val recover: (Throwable) -> AuthenticationErrors = { throwable ->
      logger.trace(throwable) { "Token verification failed" }
      when (throwable) {
        is TokenExpiredException -> TokenExpired(throwable.localizedMessage.orEmpty()
          .ifEmpty { "The Token has expired" })
        else -> TokenInvalid("Invalid Token")
      }
    }

    return Either.catch(recover) { jwtService.verifier.verify(token) }
      .flatMap { jwt -> UserId.fromString(jwt.subject).toEither { TokenInvalid("Invalid Token") } }
      .flatMap { userId -> userWithId(userId).mapLeft { Unauthorized(it.message) } }
  }

  data class AuthContext(val user: User) : GraphQLContext
}

suspend operator fun KtorGraphQLAuthService.invoke(auth: HttpAuthHeader?) = getUser(auth)
