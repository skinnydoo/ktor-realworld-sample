package io.skinnydoo.common

import org.jetbrains.exposed.sql.Query

fun Boolean?.orFalse() = this ?: false

fun Query.isNotEmpty(): Boolean = !empty()
