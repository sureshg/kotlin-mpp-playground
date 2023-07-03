package common

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.ln
import kotlin.math.pow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

internal val DEC_FORMAT = DecimalFormat("#.##")

/** OS temp location */
val tmp: String = "${System.getProperty("java.io.tmpdir")}${File.separator}"

/** Returns the file size in a human-readable format. */
val File.displaySize
  get() = length().byteDisplaySize()

val Long.compactFmt: String
  get() = NumberFormat.getCompactNumberInstance().format(this)

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

/** Find the file ends with given [format] under the directory. */
fun File.findPkg(format: String?) =
    when (format != null) {
      true -> walk().firstOrNull { it.isFile && it.name.endsWith(format, ignoreCase = true) }
      else -> null
    }

/** List files based on the glob [pattern] */
fun Path.glob(pattern: String): List<Path> {
  val matcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")
  return Files.walk(this).filter(matcher::matches).toList()
}

/** An extension function to join a multiline string for JVM arguments. */
fun String.joinToConfigString(separator: CharSequence = "") =
    trimMargin().lines().joinToString(separator) { it.trim() }

/** System property delegate */
@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T> sysProp(): ReadOnlyProperty<Any?, T> = ReadOnlyProperty { _, property ->
  val propVal = System.getProperty(property.name, "")
  val propVals = propVal.split(",", " ").filter { it.isNotBlank() }
  val kType = typeOf<T>()

  when {
    // Handle enum values
    kType.isSubtypeOf(typeOf<Enum<*>?>()) ->
        T::class.java.enumConstants.filterIsInstance<Enum<*>>().singleOrNull { it.name == propVal }

    // Handle primitive and collection types
    else ->
        when (kType) {
          typeOf<String>() -> propVal
          typeOf<Int>() -> propVal.toInt()
          typeOf<Boolean>() -> propVal.toBoolean()
          typeOf<Long>() -> propVal.toLong()
          typeOf<Double>() -> propVal.toDouble()
          typeOf<List<String>>() -> propVals
          typeOf<List<Int>>() -> propVals.map { it.toInt() }
          typeOf<List<Long>>() -> propVals.map { it.toLong() }
          typeOf<List<Double>>() -> propVals.map { it.toDouble() }
          typeOf<List<Boolean>>() -> propVals.map { it.toBoolean() }
          else -> error("'${property.name}' system property type ($kType) is not supported!")
        }
  }
      as T
}
