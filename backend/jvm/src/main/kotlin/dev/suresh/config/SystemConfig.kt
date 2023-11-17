package dev.suresh.config

import io.ktor.server.config.*
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf
import kotlin.time.Duration

data object SysConfig {

  // Logback log directory
  val LOG_DIR by lazy {
    val os = System.getProperty("os.name")
    when {
      os.startsWith("Mac", ignoreCase = true) -> System.getProperty("user.dir")
      else -> System.getenv("LOG_DIR").orEmpty().ifBlank { "/log" }
    }
  }

  /**
   * Initializes the system properties required for the application to run. This should be invoked
   * before the Engine main() method is called.
   */
  fun initSysProperty() {
    System.setProperty("jdk.tls.maxCertificateChainLength", "15")
    System.setProperty("jdk.includeInExceptions", "hostInfo")
    System.setProperty("LOG_DIR", LOG_DIR)
  }
}

/**
 * Extension function to get and convert config values to their respective type. Nullability is
 * disabled to support java types
 */
context(ApplicationConfig)
@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T> prop(prop: String) =
    when (typeOf<T>().withNullability(false)) {
      typeOf<String>() -> property(prop).getString()
      typeOf<List<String>>() -> property(prop).getList()
      typeOf<Boolean>() -> property(prop).getString().toBoolean()
      typeOf<Int>() -> property(prop).getString().toInt()
      typeOf<Long>() -> property(prop).getString().toLong()
      typeOf<Double>() -> property(prop).getString().toDouble()
      typeOf<Duration>() -> Duration.parse(property(prop).getString().lowercase())
      else -> throw IllegalArgumentException("Unsupported type: ${typeOf<T>()}")
    }
        as T
