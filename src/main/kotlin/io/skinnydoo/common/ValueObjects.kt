package io.skinnydoo.common

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
value class UserId(val value: UUID) {

  override fun toString(): String = value.toString()

  companion object {
    fun fromString(value: String): Option<UserId> =
      Option.catch { UUID.fromString(value) }.map(::UserId)
  }
}

@JvmInline
value class Slug(val value: UUID) {

  override fun toString(): String = value.toString()

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

@JvmInline
@Serializable
value class Limit(val value: Int = 20) {

  init {
    require(value >= 0) { "limit must be >= 0" }
  }

  operator fun plus(limit: Limit) = Limit(this.value + limit.value)

  operator fun minus(limit: Limit) = Limit(this.value - limit.value)

  operator fun times(num: Int) = Limit(this.value * num)

  override fun toString(): String = value.toString()

  companion object {
    val identity by lazy { Limit(0) }
    val default by lazy { Limit(20) }
    fun fromInt(value: Int): Option<Limit> = if (value >= 0) Limit(value).some() else none()
  }
}

@JvmInline
@Serializable
value class Offset(val value: Int = 0) {

  init {
    require(value >= 0) { "offset must be >= 0" }
  }

  operator fun plus(offset: Offset) = Offset(this.value + offset.value)

  operator fun minus(offset: Offset) = Offset(this.value - offset.value)

  operator fun times(num: Int) = Offset(this.value * num)

  override fun toString(): String = value.toString()

  companion object {
    val identity by lazy { Offset(0) }
    val default by lazy { Offset(0) }
    fun fromInt(value: Int): Option<Offset> = if (value >= 0) Offset(value).some() else none()
  }
}
