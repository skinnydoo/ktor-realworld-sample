package io.skinnydoo.graphql.scalars

import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import java.math.BigDecimal
import java.math.BigInteger

class GraphqlLongCoercing : Coercing<Long, Long> {

  private val longMin = BigInteger.valueOf(Long.MIN_VALUE)
  private val longMax = BigInteger.valueOf(Long.MAX_VALUE)

  override fun serialize(dataFetcherResult: Any): Long {
    return convertImpl(dataFetcherResult)
      ?: throw CoercingSerializeException("Expected type 'Long' but was '${CoercingUtil.typeName(dataFetcherResult)}'.")
  }

  /**
   * Parse Long from variable
   */
  override fun parseValue(input: Any): Long {
    return convertImpl(input)
      ?: throw CoercingParseValueException("Expected type 'Long' but was '${CoercingUtil.typeName(input)}'.")
  }

  /**
   * Parse Long from AST Literal
   */
  override fun parseLiteral(input: Any): Long = when (input) {
      is StringValue -> runCatching { input.value.toLong() }
        .getOrElse { throw CoercingParseLiteralException("Expected value to be a Long but it was '$input'") }
      is IntValue -> {
        val value = input.value
        if (value !in longMin..longMax)
          throw CoercingParseLiteralException("Expected value to be in the Long range but it was '$value'")
        value.toLong()
      }
      else -> throw CoercingParseLiteralException("Expected AST type 'IntValue' or 'StringValue' but it was '${
        CoercingUtil.typeName(input)
      }'.")
    }

  private fun convertImpl(input: Any): Long? = when {
    input is Long -> input
    CoercingUtil.isNumberIsh(input) -> runCatching { BigDecimal(input.toString()) }
      .mapCatching { value -> value.longValueExact() }
      .getOrNull()
    else -> null
  }
}
