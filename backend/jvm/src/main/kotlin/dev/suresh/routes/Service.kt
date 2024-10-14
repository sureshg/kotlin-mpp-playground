package dev.suresh.routes

import dev.suresh.JFR
import dev.suresh.http.MediaApiClient
import dev.suresh.lang.FFM
import dev.suresh.lang.VThread
import dev.suresh.log.RespLogger
import dev.suresh.plugins.custom.CookieSession
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.opentelemetry.instrumentation.annotations.WithSpan

private val logger = KotlinLogging.logger {}

fun Routing.services() {
  get("/ffm") { call.respondLogStream { FFM.memoryLayout(this) } }

  get("/vthreads") { call.respondLogStream { VThread.virtualThreads(this) } }

  get("/jfr") { call.respondLogStream { JFR.recordingStream(this) } }

  get("/trace") {
    call.respond(
        mapOf("OpenTelemetry" to BuildConfig.otelInstr, "Image Size" to mediaApiCall().toString()))
  }

  route("/session") {
    get("/set") {
      call.sessions.set(CookieSession("${BuildConfig.name}: ${BuildConfig.version}"))
      call.respondText("Session created")
    }

    get("/") {
      val session = call.sessions.get<CookieSession>()
      call.respondText("Current Session: $session")
    }
  }
}

@WithSpan suspend fun mediaApiCall() = MediaApiClient().images().size

suspend fun ApplicationCall.respondLogStream(
    contentType: ContentType = ContentType.Text.EventStream,
    block: suspend KLogger.() -> Unit
) {
  respondTextWriter(contentType = contentType) { block(RespLogger(this, logger)) }
}
