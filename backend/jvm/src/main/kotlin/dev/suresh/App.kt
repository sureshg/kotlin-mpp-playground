package dev.suresh

import BuildConfig
import dev.suresh.config.AppConfig
import dev.suresh.plugins.*
import dev.suresh.routes.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlin.io.path.Path
import kotlin.io.path.exists

fun main(args: Array<String>) =
    try {
      initProps()
      EngineMain.main(args)
    } catch (e: Throwable) {
      val log = KtorSimpleLogger("main")
      log.error("Failed to start ${BuildConfig.description}: ${e.message}", e)
    }

fun Application.module() {
  log.info("Starting ${BuildConfig.description} v${BuildConfig.version}...")
  AppConfig.init(environment.config)
  configureOTel()
  configureInterceptors()
  configureHTTP()
  configureSecurity()
  errorRoutes()

  routing {
    adminRoutes()
    webApp()
    services()
    mgmtRoutes()
  }
  // CoroutineScope(coroutineContext).launch {}
}

/**
 * Initializes the system properties required for the application to run. This should be invoked
 * before the Engine main() method is called.
 */
fun initProps() {
  val logDir =
      System.getProperty("LOG_DIR", System.getenv("LOG_DIR")).orEmpty().ifBlank {
        when {
          Path("/log").exists() -> "/log"
          else -> System.getProperty("user.dir")
        }
      }

  System.setProperty("jdk.tls.maxCertificateChainLength", "15")
  System.setProperty("jdk.includeInExceptions", "hostInfo")
  System.setProperty("slf4j.internal.verbosity", "WARN")
  System.setProperty("LOG_DIR", logDir)

  println("⚡ ${BuildConfig.description} v${BuildConfig.version} ⚡")
  println("Log Dir: $logDir")

  // Redirect JUL to SLF4J
  // SLF4JBridgeHandler.removeHandlersForRootLogger()
  // SLF4JBridgeHandler.install()
}
