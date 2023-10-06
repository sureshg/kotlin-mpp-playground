package dev.suresh

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
  EngineMain.main(args)
}

fun Application.module() {
  routing {
    singlePageApplication {
      useResources = true
      filesPath = "webApp"
      defaultPage = "index.html"
      ignoreFiles { it.endsWith(".log") }
    }
  }
}
