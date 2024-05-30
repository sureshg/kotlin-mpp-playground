package dev.suresh

import java.io.File
import java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.Semaphore
import java.util.concurrent.ThreadFactory
import jdk.jfr.Event
import jdk.jfr.FlightRecorder
import kotlin.jvm.optionals.getOrNull
import kotlin.math.ln
import kotlin.math.pow
import kotlin.reflect.KProperty

val DEC_FORMAT = DecimalFormat("#.##")

/** Returns the method name contains this call-site */
inline val methodName
  get() =
      StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk {
        it.findFirst().getOrNull()?.methodName
      }

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

/** Returns the jar file path of the class */
val <T : Class<*>> T.jarPath
  get() = protectionDomain.codeSource.location.toURI().path

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

/** Adds a periodic event to the JFR stream. */
inline fun <reified T : Event> addPeriodicJFREvent(event: T, crossinline block: T.() -> Unit) {
  FlightRecorder.addPeriodicEvent(T::class.java) {
    block(event)
    event.commit()
  }
}

fun semaphoreThreadFactory(s: Semaphore, tf: ThreadFactory = Thread.ofVirtual().factory()) =
    ThreadFactory {
      try {
        s.acquire()
        tf.newThread {
          try {
            it.run()
          } finally {
            s.release()
          }
        }
      } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        throw RuntimeException(e)
      }
    }

/** System env var property delegate. */
object Env {
  operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
      System.getenv(property.name) ?: error("${property.name} not passed as environment")
}
