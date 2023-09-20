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

  override fun env(key: String, def: String?): String? = System.getenv(key) ?: def

  override fun sysProp(key: String, def: String?): String? = System.getProperty(key, def)

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

  override val osInfo: Map<String, String?>
    get() =
        super.osInfo +
            mapOf(
                "name" to sysProp("os.name"),
                "version" to sysProp("os.version"),
                "arch" to sysProp("os.arch"),
                "user" to sysProp("user.name"),
            )
}
