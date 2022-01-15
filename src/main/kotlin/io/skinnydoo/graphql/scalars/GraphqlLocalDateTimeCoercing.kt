package io.skinnydoo.graphql.scalars

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import io.skinnydoo.graphql.scalars.CoercingUtil.typeName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GraphqlLocalDateTimeCoercing(
  private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT,
) : Coercing<LocalDateTime, String> {

  override fun serialize(input: Any): String = when (input) {
    is LocalDateTime -> runCatching { input.format(formatter) }
      .getOrElse { throw CoercingSerializeException("Unable to convert '$input' into 'java.time.LocalDateTime'", it) }
    is String -> runCatching { LocalDateTime.parse(input, formatter) }
      .onFailure { throw CoercingSerializeException("Unable to convert '$input' into 'java.time.LocalDateTime'", it) }
      .mapCatching { it.format(formatter) }
      .getOrElse { throw CoercingSerializeException("Unable to convert '$input' into 'java.time.LocalDateTime'", it) }
    else -> throw CoercingSerializeException(
      "Expected something we can convert to 'java.time.LocalDateTime' but was '${typeName(input)}'."
    )
  }

  /**
   * Parse LocalDateTime from variable
   */
  override fun parseValue(input: Any): LocalDateTime = when (input) {
    is LocalDateTime -> input
    is String -> runCatching { LocalDateTime.parse(input, formatter) }
      .getOrElse { throw CoercingParseValueException("Unable to convert '$input' into 'java.time.LocalDateTime'", it) }
    else -> throw CoercingParseValueException(
      "Expected something we can convert to 'java.time.LocalDateTime' but was '${typeName(input)}'."
    )
  }

  /**
   * Parse LocalDateTime from AST literal
   */
  override fun parseLiteral(input: Any): LocalDateTime = when (input) {
    is StringValue -> runCatching { LocalDateTime.parse(input.value, formatter) }
      .getOrElse {
        throw CoercingParseLiteralException("Unable to convert '$input' into 'java.time.LocalDateTime'", it)
      }
    else -> throw CoercingParseLiteralException("Expected AST type 'StringValue' but was '${typeName(input)}'.")
  }
}
