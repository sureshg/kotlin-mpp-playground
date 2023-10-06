package dev.suresh.routes

import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.webApp() {
  val webAppRoute = "/app"
  staticResources(webAppRoute, "webapp") {
    exclude { it.path.endsWith(".log") }
    default("index.html")
    modify { _, call ->
      when (call.request.path()) {
        webAppRoute -> call.respondRedirect("$webAppRoute/")
      }
    }
  }
}
