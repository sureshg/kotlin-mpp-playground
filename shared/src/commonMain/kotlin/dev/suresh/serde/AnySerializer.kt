package dev.suresh.serde

import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.uuid.Uuid
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*

val BUILTIN_SERIALIZERS: Map<KClass<*>, KSerializer<*>> by lazy {
  mapOf(
      String::class to String.serializer(),
      Char::class to Char.serializer(),
      CharArray::class to CharArraySerializer(),
      Double::class to Double.serializer(),
      DoubleArray::class to DoubleArraySerializer(),
      Float::class to Float.serializer(),
      FloatArray::class to FloatArraySerializer(),
      Long::class to Long.serializer(),
      LongArray::class to LongArraySerializer(),
      ULong::class to ULong.serializer(),
      ULongArray::class to ULongArraySerializer(),
      Int::class to Int.serializer(),
      IntArray::class to IntArraySerializer(),
      UInt::class to UInt.serializer(),
      UIntArray::class to UIntArraySerializer(),
      Short::class to Short.serializer(),
      ShortArray::class to ShortArraySerializer(),
      UShort::class to UShort.serializer(),
      UShortArray::class to UShortArraySerializer(),
      Byte::class to Byte.serializer(),
      ByteArray::class to ByteArraySerializer(),
      UByte::class to UByte.serializer(),
      UByteArray::class to UByteArraySerializer(),
      Boolean::class to Boolean.serializer(),
      BooleanArray::class to BooleanArraySerializer(),
      Unit::class to Unit.serializer(),
      Nothing::class to NothingSerializer(),
      Duration::class to Duration.serializer(),
      Uuid::class to Uuid.serializer())
}

fun KClass<*>?.builtinSerializerOrNull() = BUILTIN_SERIALIZERS[this]

@OptIn(InternalSerializationApi::class)
fun Any?.toJsonElement(): JsonElement {
  if (this == null) return JsonNull

  @Suppress("UNCHECKED_CAST")
  val serializer =
      (this::class.builtinSerializerOrNull() ?: this::class.serializerOrNull()) as? KSerializer<Any>

  // val jvmSerializer = Json.serializersModule.serializerOrNull(this::class.java)

  return when {
    serializer != null -> Json.encodeToJsonElement(serializer, this)
    this is Map<*, *> -> toJsonElement()
    this is Array<*> -> toJsonElement()
    this is Collection<*> -> toJsonElement()
    this is Number -> JsonPrimitive(this)
    this is Enum<*> -> JsonPrimitive(this.name)
    this is Pair<*, *> -> toJsonElement()
    this is Triple<*, *, *> -> toJsonElement()
    else -> error("Can't serialize '$this' as it is of an unknown type")
  }
}

fun Map<*, *>.toJsonElement(): JsonElement = buildJsonObject {
  forEach { (key, value) ->
    if (key !is String) {
      error("Only string keys are supported for maps")
    }
    put(key, value.toJsonElement())
  }
}

fun Collection<*>.toJsonElement(): JsonElement = buildJsonArray {
  forEach { add(it.toJsonElement()) }
}

fun Array<*>.toJsonElement(): JsonElement = buildJsonArray { forEach { add(it.toJsonElement()) } }

fun BooleanArray.toJsonElement(): JsonElement = buildJsonArray {
  forEach { add(JsonPrimitive(it)) }
}

fun ByteArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun CharArray.toJsonElement(): JsonElement = buildJsonArray {
  forEach { add(JsonPrimitive(it.toString())) }
}

fun ShortArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun IntArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun LongArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun FloatArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun DoubleArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun UByteArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun UShortArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun UIntArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun ULongArray.toJsonElement(): JsonElement = buildJsonArray { forEach { add(JsonPrimitive(it)) } }

fun Pair<*, *>.toJsonElement(): JsonElement = buildJsonObject {
  put("first", first.toJsonElement())
  put("second", second.toJsonElement())
}

fun Triple<*, *, *>.toJsonElement(): JsonElement = buildJsonObject {
  put("first", first.toJsonElement())
  put("second", second.toJsonElement())
  put("third", third.toJsonElement())
}
