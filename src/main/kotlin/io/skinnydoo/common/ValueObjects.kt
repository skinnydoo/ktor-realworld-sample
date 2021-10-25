package io.skinnydoo.common

import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class Slug(val value: UUID)

@JvmInline
@Serializable
value class Email(val value: String)

@JvmInline
@Serializable
value class Username(val value: String)

@JvmInline
@Serializable
value class Password(val value: String)
