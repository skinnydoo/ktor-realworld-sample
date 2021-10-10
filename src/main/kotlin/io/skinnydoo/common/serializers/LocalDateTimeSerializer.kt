package io.skinnydoo.common.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {

  private val formatter by lazy { DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withLocale(Locale.ROOT) }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LocalDateTime) {
    encoder.encodeString(ZonedDateTime.of(value, ZoneOffset.systemDefault())
      .withZoneSameInstant(ZoneOffset.UTC)
      .format(formatter))
  }

  override fun deserialize(decoder: Decoder): LocalDateTime {
    return ZonedDateTime.of(LocalDateTime.parse(decoder.decodeString(), formatter), ZoneOffset.UTC)
      .withZoneSameInstant(ZoneId.systemDefault())
      .toLocalDateTime()
  }
}

object NullableLocalDateTimeSerializer : KSerializer<LocalDateTime?> {

  private val delegate = LocalDateTimeSerializer.nullable

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("NullableLocalDateTime", PrimitiveKind.STRING).nullable

  override fun serialize(encoder: Encoder, value: LocalDateTime?) {
    if (value == null) {
      delegate.serialize(encoder, null)
    } else {
      delegate.serialize(encoder, value)
    }
  }

  override fun deserialize(decoder: Decoder): LocalDateTime? {
    return try {
      delegate.deserialize(decoder)
    } catch (ex: Exception) {
      null
    }
  }
}
