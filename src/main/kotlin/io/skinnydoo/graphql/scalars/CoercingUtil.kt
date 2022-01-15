package io.skinnydoo.graphql.scalars

object CoercingUtil {

  fun isNumberIsh(input: Any): Boolean = input is Number || input is String

  fun typeName(input: Any?): String = input?.javaClass?.simpleName ?: input.toString()
}
