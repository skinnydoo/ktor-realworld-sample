package io.skinnydoo.graphql

import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.skinnydoo.common.models.User

data class AuthContext(val user: User) : GraphQLContext
