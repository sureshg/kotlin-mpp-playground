package dev.suresh

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import org.slf4j.LoggerFactory

actual fun platform(): Platform = JvmPlatform

object JvmPlatform : Platform {

  private val log = LoggerFactory.getLogger(this::class.java)

  override val name: String = "JVM"

  override val javaRuntimeVersion: String = System.getProperty("java.runtime.version")

  override val tzShortId: String
    get() {
      // ZoneId.systemDefault().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH)
      return DateTimeFormatter.ofPattern("zzz")
          .withLocale(Locale.ENGLISH)
          .withZone(ZoneId.systemDefault())
          .format(Instant.now())
    }
  /** A coroutine dispatcher that executes tasks on Virtual Threads. */
  override val vtDispatcher by lazy {
    log.info("Creating CoroutineDispatcher based on Java VirtualThreadPerTaskExecutor...")
    Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
  }

  override fun env(key: String): String? = System.getenv(key)
}
