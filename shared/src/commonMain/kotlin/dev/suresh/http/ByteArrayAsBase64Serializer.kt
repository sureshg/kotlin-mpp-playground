package dev.suresh.http

import kotlin.io.encoding.Base64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A [Base64] serializer. Use with
 *
 * ```kotlin
 * @Serializable(with = ByteArrayAsBase64Serializer::class)
 * ```
 */
object ByteArrayAsBase64Serializer : KSerializer<ByteArray> {

  private val base64 = Base64.Default

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("ByteArrayAsBase64Serializer", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): ByteArray {
    val base64Decoded = decoder.decodeString()
    return base64.decode(base64Decoded)
  }

  override fun serialize(encoder: Encoder, value: ByteArray) {
    val base64Encoded = base64.encode(value)
    encoder.encodeString(base64Encoded)
  }
}
