package dev.suresh

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array

/**
 * Convert JS [ArrayBuffer] to Kotlin [ByteArray]
 *
 * [Kotlin-JS-Types](https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript)
 */
fun ArrayBuffer?.asByteArray() = this?.run { Int8Array(this).unsafeCast<ByteArray>() }
