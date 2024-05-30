package dev.suresh

import kotlinx.browser.window
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array

actual val platform: Platform = JsPlatform

object JsPlatform : Platform {
  override val name: String = "JS"

  override val osInfo: Map<String, String?>
    get() =
        super.osInfo +
            mapOf(
                "name" to window.navigator.platform,
                "userAgent" to window.navigator.userAgent,
                "vendor" to window.navigator.vendor,
            )
}

/**
 * Convert JS [ArrayBuffer] to Kotlin [ByteArray]
 *
 * [Kotlin-JS-Types](https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript)
 */
fun ArrayBuffer?.asByteArray() = this?.run { Int8Array(this).unsafeCast<ByteArray>() }
