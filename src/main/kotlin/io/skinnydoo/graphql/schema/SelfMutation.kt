package io.skinnydoo.graphql.schema

import com.expediagroup.graphql.server.operations.Mutation
import graphql.schema.DataFetchingEnvironment
import io.skinnydoo.common.models.UpdateSelfResult
import io.skinnydoo.users.UpdateUser

class SelfMutation(
  private val updateUser: UpdateUser,
) : Mutation {

  suspend fun updateSelf(
    dfe: DataFetchingEnvironment,
    email: String?,
    password: String?,
    bio: String?,
    image: String?,
  ): UpdateSelfResult {

    TODO()
  }
}
