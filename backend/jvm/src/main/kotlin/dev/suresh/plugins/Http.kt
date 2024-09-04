package dev.suresh.plugins

import BuildConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.request.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlinx.atomicfu.atomic
import org.slf4j.event.Level

const val TRACE_ID = "trace-id"

private val counter = atomic(1L)

fun Application.configureHTTP() {

  install(Resources)

  install(ContentNegotiation) { json(dev.suresh.http.json) }

  install(IgnoreTrailingSlash)

  install(PartialContent)

  install(AutoHeadResponse)

  install(ForwardedHeaders)

  install(XForwardedHeaders)

  install(DefaultHeaders) { header("X-Engine", "${BuildConfig.name}-${BuildConfig.version}") }

  install(Compression) {
    gzip { priority = 10.0 }
    deflate {
      priority = 1.0
      minimumSize(1024) // condition
    }
  }

  install(CORS) {
    anyHost()
    allowHeader(HttpHeaders.ContentType)
    exposeHeader("Location")
    exposeHeader("Server")
    allowCredentials = true
  }

  install(HSTS)

  install(CallId) {
    header(HttpHeaders.XRequestId)
    generate {
      when (it.isApi) {
        true -> "$TRACE_ID-${counter.getAndIncrement()}"
        else -> "$TRACE_ID-00000"
      }
    }
    verify { it.isNotEmpty() }
  }

  install(CallLogging) {
    level = Level.INFO
    disableForStaticContent()
    disableDefaultColors()

    // Add MDC entries
    mdc("remoteHost") { call -> call.request.origin.remoteHost }
    callIdMdc(TRACE_ID)

    // Enable logging for API routes only
    filter { it.isApi }
  }

  install(WebSockets) {
    pingPeriod = 15.seconds.toJavaDuration()
    timeout = 15.seconds.toJavaDuration()
    maxFrameSize = Long.MAX_VALUE
    masking = false
  }
}

fun Application.configureInterceptors() {
  intercept(ApplicationCallPipeline.Plugins) {
    println("Request: ${call.request.uri}")
    if (call.request.headers["Custom-Header"] == "Test") {
      call.respond(HttpStatusCode.Forbidden)
      finish()
    }
  }
}

val ApplicationCall.debug
  get() = request.queryParameters.contains("debug")

val ApplicationCall.isApi
  get() = run {
    val path = request.path()
    when {
      path.contains("/swagger") -> false
      path.startsWith("/api/") -> true
      else -> false
    }
  }
