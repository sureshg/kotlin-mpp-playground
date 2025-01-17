package dev.suresh.serde

import kotlin.io.encoding.Base64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

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

  override fun deserialize(decoder: Decoder) = base64.decode(decoder.decodeString())

  override fun serialize(encoder: Encoder, value: ByteArray) =
      encoder.encodeString(base64.encode(value))
}
