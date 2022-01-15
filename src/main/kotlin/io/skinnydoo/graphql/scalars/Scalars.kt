package io.skinnydoo.graphql.scalars

import graphql.schema.GraphQLScalarType
import java.time.format.DateTimeFormatter
import java.util.*

object Scalars {

  val GraphQLLong: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("Long")
    .description("A 64-bit signed integer")
    .coercing(GraphqlLongCoercing())
    .build()

  val GraphQLDateTime: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("DateTime")
    .description("A DateTime Scalar")
    .coercing(GraphqlLocalDateTimeCoercing(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      .withLocale(Locale.ROOT)))
    .build()
}
