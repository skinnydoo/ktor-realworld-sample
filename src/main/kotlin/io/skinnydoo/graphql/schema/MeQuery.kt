@file:Suppress("unused")

package io.skinnydoo.graphql.schema

import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import io.skinnydoo.common.models.LoggedInUser
import io.skinnydoo.common.models.User

class MeQuery : Query {

  fun me(dfe: DataFetchingEnvironment): LoggedInUser {
    val user = checkNotNull(dfe.graphQlContext.get<User>("user")) { "Invalid Token" }
    return LoggedInUser.fromUser(user, "")
  }
}
