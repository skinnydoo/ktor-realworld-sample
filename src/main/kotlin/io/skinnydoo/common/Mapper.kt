package io.skinnydoo.common

interface Mapper<in F, out T> {
  fun map(from: F): T
}
