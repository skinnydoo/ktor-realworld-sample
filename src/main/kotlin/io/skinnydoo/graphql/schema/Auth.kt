@file:Suppress("unused")

package io.skinnydoo.graphql.schema

import arrow.core.identity
import com.expediagroup.graphql.server.operations.Mutation
import io.skinnydoo.common.*
import io.skinnydoo.common.models.*
import io.skinnydoo.users.LoginUser
import io.skinnydoo.users.RegisterUser

class LoginMutationService(
  private val loginWithEmail: LoginUser,
  private val jwtService: JwtService,
) : Mutation {

  suspend fun login(email: String, password: String): LoginResult {
    return loginWithEmail(UserLoginCredentials(Email(email), Password(password))).map {
      LoggedInUser.fromUser(it, token = jwtService.generateToken(it))
    }.fold(::identity, ::identity)
  }
}

class RegisterMutationService(
  private val registerUser: RegisterUser,
  private val jwtService: JwtService,
) : Mutation {

  suspend fun register(username: String, email: String, password: String): RegisterResult {
    val newUser = NewUserCredentials(Username(username), Email(email), Password(password))
    val hashedPassword = Password(hash(newUser.password.value))
    val newUserWithHashedPassword = newUser.copy(password = hashedPassword)

    return registerUser(newUserWithHashedPassword).map { LoggedInUser.fromUser(it, jwtService.generateToken(it)) }
      .fold(::identity, ::identity)
  }
}
