@file:Suppress("unused")

package io.skinnydoo.graphql.schema

import arrow.core.identity
import com.expediagroup.graphql.server.operations.Mutation
import io.skinnydoo.common.Email
import io.skinnydoo.common.JwtService
import io.skinnydoo.common.Password
import io.skinnydoo.common.models.LoggedInUser
import io.skinnydoo.common.models.LoginResult
import io.skinnydoo.common.models.UserLoginCredentials
import io.skinnydoo.users.LoginUser

class LoginMutation(
  private val loginWithEmail: LoginUser,
  private val jwtService: JwtService,
) : Mutation {

  suspend fun login(email: String, password: String): LoginResult {
    return loginWithEmail(UserLoginCredentials(Email(email), Password(password))).map {
      LoggedInUser.fromUser(it, token = jwtService.generateToken(it))
    }.fold(::identity, ::identity)
  }
}
