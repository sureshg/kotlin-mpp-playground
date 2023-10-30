package dev.suresh.routes

import dev.suresh.jvmRuntimeInfo
import dev.suresh.plugins.debug
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val DEBUG = ScopedValue.newInstance<Boolean>()

fun Route.webApp() {
  val webAppRoute = "/app"

  get("/info") {
    call.respond(ScopedValue.where(DEBUG, call.debug).get { jvmRuntimeInfo(DEBUG.get()) })
  }

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
