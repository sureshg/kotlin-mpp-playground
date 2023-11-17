package dev.suresh.routes

import BuildConfig
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes() {
  swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
    version = BuildConfig.swaggerUi
  }

  get("/") { call.respondRedirect("/swagger") }
}
