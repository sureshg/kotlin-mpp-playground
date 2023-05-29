package dev.suresh.gradle

import java.io.File
import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.pow

/** OS temp location */
val tmp: String = "${System.getProperty("java.io.tmpdir")}${File.separator}"

internal val DEC_FORMAT = DecimalFormat("#.##")

/**
 * Returns a human-readable version of the Byte size, where the input represents a specific number
 * of bytes.
 * - [SI vs Binary](https://en.wikipedia.org/wiki/Template:Bit_and_byte_prefixes)
 * - [SI](https://en.wikipedia.org/wiki/International_System_of_Units#Prefixes)
 * - [Binary](https://en.wikipedia.org/wiki/Binary_prefix)
 */
fun Long.byteDisplaySize(si: Boolean = true): String {
  require(this >= 0) { "Bytes can't be negative" }
  val unit = if (si) 1000 else 1024
  return when (this < unit) {
    true -> "$this B"
    else -> {
      val exp = (ln(toDouble()) / ln(unit.toDouble())).toInt()
      val siSymbol = "kMGTPEZY"[exp - 1].toString()
      val prefix = if (si) siSymbol else "${siSymbol.uppercase()}i"
      val size = this / unit.toDouble().pow(exp)
      "${DEC_FORMAT.format(size)} ${prefix}B"
    }
  }
}
