package io.skinnydoo.common

import arrow.core.Option
import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
value class UserId(val value: UUID) {

  companion object {
    fun fromString(value: String): Option<UserId> =
      Option.catch { UUID.fromString(value) }.map(::UserId)
  }
}

@JvmInline
value class Slug(val value: UUID) {

  companion object {
    fun fromString(value: String): Option<Slug> =
      Option.catch { UUID.fromString(value) }.map(::Slug)
  }
}

@JvmInline
@Serializable
value class Email(val value: String)

@JvmInline
@Serializable
value class Username(val value: String)

@JvmInline
@Serializable
value class Password(val value: String)
