package io.skinnydoo.common

import arrow.core.identity
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.ApplicationEnvironment
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.jwt.jwt
import io.skinnydoo.users.User
import org.mindrot.jbcrypt.BCrypt
import java.time.Duration
import java.util.Date

data class JWTConfig(
  val issuer: String,
  val audience: String,
  val realm: String,
  val secret: String,
  val validity: Duration,
)

class JwtService(private val jwtConfig: JWTConfig) {

  private val algorithm = Algorithm.HMAC256(jwtConfig.secret)
  val realm = jwtConfig.realm

  val verifier: JWTVerifier = JWT.require(algorithm)
    .withIssuer(jwtConfig.issuer)
    .withAudience(jwtConfig.audience)
    .build()

  fun generateToken(user: User): String = JWT.create()
    .withSubject("Authentication")
    .withIssuer(jwtConfig.issuer)
    .withAudience(jwtConfig.audience)
    .withClaim("id", user.id.toString())
    // .withExpiresAt(expiresAt())
    .sign(algorithm)

  private fun expiresAt() = Date(System.currentTimeMillis() + jwtConfig.validity.toMillis())
}

fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

fun checkPassword(candidate: String, hashed: String): Boolean = BCrypt.checkpw(candidate, hashed)

fun ApplicationEnvironment.jwtConfig(path: String): JWTConfig = with(config.config(path)) {
  JWTConfig(
    issuer = property("issuer").getString(),
    audience = property("audience").getString(),
    realm = property("realm").getString(),
    secret = property("secret").getString(),
    validity = Duration.ofMillis(property("validity_ms").getString().toLong())
  )
}

fun Authentication.Configuration.configure(jwtService: JwtService, validate: suspend (UserId) -> Principal?) {
  jwt(name = "auth-jwt") {
    realm = jwtService.realm
    authSchemes("Token")
    verifier(jwtService.verifier)
    this.validate { credential ->
      credential.payload
        .getClaim("id")
        .asString()
        ?.let { id -> UserId.fromString(id).toEither { null }.fold(::identity) { validate(it) } }
    }
  }
}
