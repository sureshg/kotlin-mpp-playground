package dev.suresh

import BuildConfig
import dev.suresh.config.SysConfig
import dev.suresh.plugins.configureHTTP
import dev.suresh.plugins.configureOTel
import dev.suresh.plugins.configureSecurity
import dev.suresh.plugins.custom.customPlugins
import dev.suresh.plugins.errorRoutes
import dev.suresh.routes.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*

fun main(args: Array<String>) =
    try {
      SysConfig.initSysProperties()
      println("Starting App ${BuildConfig.version}...")
      EngineMain.main(args)
    } catch (e: Throwable) {
      val log = KtorSimpleLogger("main")
      log.error("Failed to start the application: ${e.message}", e)
    }

fun Application.module() {
  configureHTTP()
  configureSecurity()
  configureOTel()
  errorRoutes()
  customPlugins()
  routing {
    adminRoutes()
    webApp()
    services()
    mgmtRoutes()
  }
  // CoroutineScope(coroutineContext).launch {}
}
