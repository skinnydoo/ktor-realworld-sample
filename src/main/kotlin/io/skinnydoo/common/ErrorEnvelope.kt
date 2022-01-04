package io.skinnydoo.common

import kotlinx.serialization.Serializable

@Serializable
data class ErrorEnvelope<T>(val errors: T)
