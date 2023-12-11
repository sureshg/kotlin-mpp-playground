package dev.suresh.routes

import BuildConfig
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {
  swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
    version = BuildConfig.swaggerUi
    customStyle("https://unpkg.com/swagger-ui-themes@3.0.1/themes/3.x/theme-flattop.css")
  }

  get("/") { call.respondRedirect("/swagger") }
}
