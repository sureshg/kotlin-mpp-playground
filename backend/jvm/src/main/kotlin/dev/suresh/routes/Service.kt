package dev.suresh.routes

import dev.suresh.JFR
import dev.suresh.http.*
import dev.suresh.lang.*
import dev.suresh.log.RespLogger
import dev.suresh.plugins.custom.AuthzPlugin
import dev.suresh.plugins.custom.CookieSession
import dev.suresh.wasm.wasm
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.http.content.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.hide
import io.ktor.server.sessions.*
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.send
import io.ktor.server.sse.sse
import io.ktor.server.websocket.webSocket
import io.ktor.sse.ServerSentEvent
import io.ktor.utils.io.ExperimentalKtorApi
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import io.opentelemetry.instrumentation.annotations.WithSpan
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.serializer

private val logger = KotlinLogging.logger {}

@OptIn(ExperimentalKtorApi::class)
fun Routing.services() {
  /** Tag: Service */
  get("/ffm") { call.respondLogStream { FFM.memoryLayout(this) } }

  /** Tag: Service */
  get("/vthreads") { call.respondLogStream { VThread.virtualThreads(this) } }

  /** Tag: Service */
  get("/jfr") { call.respondLogStream { JFR.recordingStream(this) } }.hide()

  /** Tag: Service */
  get("/long-running") {
    try {
      while (call.isActive) {
        delay(2.seconds)
        logger.info { "Long running task..." }
      }
      call.respondText("Completed!")
    } catch (e: CancellationException) {
      logger.info { "Long running task cancelled: ${e.message}" }
    }
  }

  /** Tag: Service */
  get("/trace") {
    call.respond(
        mapOf("OpenTelemetry" to BuildConfig.otelInstr, "Image Size" to mediaApiCall().toString())
    )
  }
  /** Tag: Service */
  get("/otel-config") { call.respondResource("otel/sdk-config.yaml") }

  /** Tag: Service */
  route("/session") {
    get("/set") {
      call.sessions.set(CookieSession("${BuildConfig.name}: ${BuildConfig.version}"))
      call.respondText("Session created")
    }

    get {
      val session = call.sessions.get<CookieSession>()
      call.respondText("Current Session: $session")
    }
  }

  /** Tag: Service */
  route("/csrf") {
    install(CSRF) {
      allowOrigin("https://localhost:8080")
      originMatchesHost()
      checkHeader("X-CSRF") { csrfHeader ->
        val originHeader = request.headers[HttpHeaders.Origin]
        csrfHeader == originHeader?.hashCode()?.toString(32)
      }

      onFailure { respondText("Access denied!", status = HttpStatusCode.Forbidden) }
    }

    post { call.respondText("CSRF check passed!") }
  }

  wasm()

  /** Tag: Service */
  webSocket("/chat") {
    send("You are connected!")
    for (frame in incoming) {
      frame as? Frame.Text ?: continue
      val receivedText = frame.readText()
      send("You said: $receivedText")
    }
  }

  /** Tag: Service */
  sse(
      "/sse",
      serialize = { typeInfo, data ->
        val serializer = json.serializersModule.serializer(typeInfo.kotlinType!!)
        json.encodeToString(serializer, data)
      },
  ) {
    val name = call.parameters["name"] ?: "World"
    var counter = 0
    while (isActive) {
      send(Name("Hello", "$name ${counter++}"))
    }

    heartbeat {
      period = 10.seconds
      event = ServerSentEvent("heartbeat")
    }

    close()
  }

  /** Tag: Service */
  get("/no-compression") {
    // Prevent response body compression
    call.suppressCompression()
    call.suppressDecompression()
    println(call.isDecompressionSuppressed)
    println(call.isCompressionSuppressed) // true
  }

  /** Tag: Service */
  route("/authz") {
    install(AuthzPlugin) {
      enabled = true
      roles = setOf("admin")
      getRole = { it ?: "admin" }
    }
    get { call.respondText("Hello, ${call.principal<UserIdPrincipal>()?.name}!") }
  }
}

@WithSpan suspend fun mediaApiCall() = MediaApiClient().images().size

suspend fun ApplicationCall.respondLogStream(
    contentType: ContentType = ContentType.Text.EventStream,
    block: suspend KLogger.() -> Unit,
) {
  respondTextWriter(contentType = contentType) { block(RespLogger(this, logger)) }
}
