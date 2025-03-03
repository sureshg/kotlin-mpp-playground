package dev.suresh.plugins

import BuildConfig
import dev.suresh.plugins.custom.CookieSession
import dev.suresh.plugins.custom.CookieSessionSerializer
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.sse.*
import io.ktor.server.websocket.*
import kotlin.concurrent.atomics.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.slf4j.event.Level

const val TRACE_ID = "trace-id"

private val counter = AtomicLong(1L)

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

  install(ConditionalHeaders) {
    //  version { call, outgoingContent ->
    //    when (outgoingContent.contentType?.withoutParameters()) {
    //      ContentType.Text.CSS ->
    //          listOf(EntityTagVersion("abc123"), LastModifiedVersion(GMTDate(123)))
    //      else -> emptyList()
    //    }
    //  }
  }

  install(Sessions) {
    cookie<CookieSession>("SESSION") {
      serializer = CookieSessionSerializer
      cookie.apply {
        secure = true
        path = "/"
        maxAge = Duration.INFINITE
        httpOnly = true
        extensions["SameSite"] = "lax"
      }
    }
  }

  install(HSTS)

  install(CallId) {
    header(HttpHeaders.XRequestId)
    generate {
      when (it.isApi) {
        true -> "$TRACE_ID-${counter.incrementAndFetch()}"
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

  install(SSE)

  install(WebSockets) {
    pingPeriod = 15.seconds
    timeout = 15.seconds
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
