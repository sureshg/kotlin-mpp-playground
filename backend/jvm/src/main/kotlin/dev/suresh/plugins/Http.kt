package dev.suresh.plugins

import BuildConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
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
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import org.slf4j.event.Level

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

  install(CallLogging) {
    level = Level.INFO
    disableForStaticContent()
    disableDefaultColors()
    filter { it.isApiRoute }
    mdc("remoteHost") { call -> call.request.origin.remoteHost }
  }

  install(WebSockets) {
    pingPeriod = 15.seconds.toJavaDuration()
    timeout = 15.seconds.toJavaDuration()
    maxFrameSize = Long.MAX_VALUE
    masking = false
  }
}

val ApplicationCall.debug
  get() = request.queryParameters.contains("debug")

val ApplicationCall.isApiRoute
  get() = request.path().startsWith("/api")
