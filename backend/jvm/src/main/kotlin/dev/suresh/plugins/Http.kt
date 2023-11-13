package dev.suresh.plugins

import BuildConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun Application.configureHTTP() {
  install(IgnoreTrailingSlash)

  install(PartialContent)

  install(AutoHeadResponse)

  install(ForwardedHeaders)

  install(XForwardedHeaders)

  install(DefaultHeaders) { header("X-Engine", "App-${BuildConfig.version}") }

  install(Compression) {
    gzip { priority = 1.0 }
    deflate {
      priority = 10.0
      minimumSize(1024) // condition
    }
  }

  install(CORS) {
    anyHost()
    allowHeader(HttpHeaders.ContentType)
  }

  install(CallLogging) {
    level = Level.INFO
    disableForStaticContent()
    disableDefaultColors()
    filter { it.isApiRoute }
    mdc("remoteHost") { call -> call.request.origin.remoteHost }
  }
}

val ApplicationCall.debug
  get() = request.queryParameters.contains("debug")

val ApplicationCall.isApiRoute
  get() = request.path().startsWith("/api")
