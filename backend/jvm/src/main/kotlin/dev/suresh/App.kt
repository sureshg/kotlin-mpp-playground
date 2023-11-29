package dev.suresh

import BuildConfig
import dev.suresh.config.SysConfig
import dev.suresh.plugins.configureHTTP
import dev.suresh.plugins.configureSecurity
import dev.suresh.plugins.configureSerialization
import dev.suresh.plugins.errorRoutes
import dev.suresh.routes.adminRoutes
import dev.suresh.routes.jvmFeatures
import dev.suresh.routes.mgmtRoutes
import dev.suresh.routes.webApp
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*

fun main(args: Array<String>) =
    try {
      SysConfig.initSysProperty()
      println("Starting App ${BuildConfig.version}...")
      EngineMain.main(args)
    } catch (e: Throwable) {
      val log = KtorSimpleLogger("main")
      log.error("Failed to start the application: ${e.message}", e)
    }

fun Application.module() {
  configureHTTP()
  configureSerialization()
  configureSecurity()
  errorRoutes()
  routing {
    adminRoutes()
    webApp()
    jvmFeatures()
    mgmtRoutes()
  }
  // CoroutineScope(coroutineContext).launch {}
}
