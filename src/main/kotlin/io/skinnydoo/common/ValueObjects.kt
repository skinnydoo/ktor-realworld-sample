package io.skinnydoo.common

import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
value class UserId(val text: UUID)

@JvmInline
@Serializable
value class Email(val text: String)

@JvmInline
@Serializable
value class Username(val text: String)

@JvmInline
@Serializable
value class Password(val text: String)
