package dev.suresh.routes

import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.webApp() {
  webApp("/app", "app")
}

fun Route.webApp(remotePath: String, basePackage: String) {
  staticResources(remotePath, basePackage) {
    exclude { it.path.endsWith(".log") }
    default("index.html")
    modify { url, call ->
      when (call.request.path()) {
        remotePath -> call.respondRedirect("$remotePath/")
      }
    }
  }
}
