package dev.suresh

import dev.suresh.plugins.configureHTTP
import dev.suresh.plugins.configureSecurity
import dev.suresh.plugins.configureSerialization
import dev.suresh.plugins.errorRoutes
import dev.suresh.routes.adminRoutes
import dev.suresh.routes.webApp
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
  EngineMain.main(args)
}

fun Application.module() {
  configureHTTP()
  configureSerialization()
  configureSecurity()
  errorRoutes()
  routing {
    adminRoutes()
    webApp()
  }
}
