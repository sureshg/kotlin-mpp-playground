package common

import java.io.File
import java.lang.reflect.Proxy
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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.*

internal val DEC_FORMAT = DecimalFormat("#.##")

/** OS temp location */
val tmp: String = "${System.getProperty("java.io.tmpdir")}${File.separator}"

/** Read the [Class] as [ByteArray] */
fun <T : Class<*>> T.toBytes() =
    classLoader.getResourceAsStream("${name.replace('.', '/')}.class")?.readBytes()

/**
 * Returns the actual class URL
 *
 * ```
 * val url = LogManager::class.java.resourcePath
 * ```
 */
val <T : Class<*>> T.resourcePath
  get() = getResource("$simpleName.class")

/** Run the lambda in the context of the receiver classloader. */
fun ClassLoader.using(run: () -> Unit) {
  val cl = Thread.currentThread().contextClassLoader
  try {
    Thread.currentThread().contextClassLoader = this
    run()
  } finally {
    Thread.currentThread().contextClassLoader = cl
  }
}

/** Return a mock object for the type, which throws exception for all method invocations. */
inline fun <reified T> mock() =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, _, _ ->
      TODO()
      // InvocationHandler.invokeDefault(proxy, method, args)
    } as T

/** Returns the file size in a human-readable format. */
val File.displaySize
  get() = length().byteDisplaySize()

val Long.compactFmt: String
  get() = NumberFormat.getCompactNumberInstance().format(this)

/** Converts a string to camelcase by splitting it by space, dash, underscore, or dot. */
val String.camelCase: String
  get() =
      split("""[.\-_ ]""".toRegex())
          .mapIndexed { idx, s -> if (idx == 0) s else s.replaceFirstChar { it.uppercaseChar() } }
          .joinToString("")

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

fun IntArray.codePointsToString(): String = buildString {
  for (cp in this@codePointsToString) {
    appendCodePoint(cp)
  }
}

fun IntArray.codePointsToString(separator: String = "") =
    joinToString(separator) { Character.toString(it) }

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

/** Check if it's a non-stable(RC) version. */
val String.isNonStable: Boolean
  get() {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { uppercase().contains(it) }
    val regex = "^[\\d,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
  }

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

/**
 * Displays the progress of a task by animating a progress symbol.
 *
 * ```kotlin
 *  // Start the work
 *  val progress = showProgress("Waiting for job to complete")
 *  // Check if work is completed
 *  progress.cancel()
 * ```
 *
 * @param message The message to be displayed alongside the progress symbol.
 */
fun CoroutineScope.showProgress(message: String) = launch {
  val progressSymbols = listOf("⣷", "⣯", "⣟", "⡿", "⢿", "⣻", "⣽", "⣾")
  launch {
    var progressIndex = 0
    while (isActive) {
      progressIndex = (progressIndex + 1) % progressSymbols.size
      val symbol = progressSymbols[progressIndex]
      print("\r[$symbol] $message")
      delay(200.milliseconds)
    }
  }
}
