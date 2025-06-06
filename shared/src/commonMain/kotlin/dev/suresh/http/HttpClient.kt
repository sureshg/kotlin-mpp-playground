package dev.suresh.http

import dev.suresh.log
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.ClassDiscriminatorMode.POLYMORPHIC
import kotlinx.serialization.json.Json

/** Common JSON instance for serde of JSON data. */
val json by lazy {
  Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
    coerceInputValues = true
    decodeEnumsCaseInsensitive = true
    allowTrailingComma = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    allowComments = true
    classDiscriminatorMode = POLYMORPHIC
  }
}

typealias HttpClientConfigurer = HttpClientConfig<*>.() -> Unit

/**
 * Multiplatform HTTP client engine configuration
 *
 * See [doc](https://ktor.io/docs/client-engines.html#mpp-config) for more details.
 */
expect fun httpClient(
    name: String = "Api Client",
    timeout: Timeout = Timeout.DEFAULT,
    retry: Retry = Retry.DEFAULT,
    kLogger: KLogger = log,
    config: HttpClientConfigurer = defaultHttpClientConfig(name, timeout, retry, kLogger)
): HttpClient

fun defaultHttpClientConfig(
    name: String,
    timeout: Timeout,
    retry: Retry,
    kLogger: KLogger
): HttpClientConfigurer = {
  install(Resources)
  install(ContentNegotiation) { json(json) }
  install(ContentEncoding) {
    deflate(1.0F)
    gzip(0.9F)
  }

  install(HttpRequestRetry) {
    maxRetries = retry.attempts
    retryOnException(retryOnTimeout = true)
    retryOnServerErrors()
    exponentialDelay(maxDelayMs = retry.maxDelay.inWholeMilliseconds)
    // modifyRequest { it.headers.append("X_RETRY_COUNT", retryCount.toString()) }
  }

  install(HttpTimeout) {
    connectTimeoutMillis = timeout.connection.inWholeMilliseconds
    requestTimeoutMillis = timeout.read.inWholeMilliseconds
    socketTimeoutMillis = timeout.write.inWholeMilliseconds
  }

  install(HttpCookies)

  install(Logging) {
    level =
        when {
          kLogger.isDebugEnabled() -> LogLevel.ALL
          kLogger.isLoggingOff() -> LogLevel.NONE
          else -> LogLevel.INFO
        }

    logger =
        object : Logger {
          override fun log(message: String) {
            kLogger.info { message }
          }
        }
    format = LoggingFormat.OkHttp
    sanitizeHeader { header -> header == HttpHeaders.Authorization }
    // filter { it.url.host.contains("localhost").not() }
  }

  engine {
    pipelining = true
    // proxy  = ProxyBuilder.http()
  }

  followRedirects = true

  install(UserAgent) { agent = "$name-${BuildConfig.version}" }

  install(DefaultRequest) {
    headers.appendIfNameAndValueAbsent(
        HttpHeaders.ContentType, ContentType.Application.Json.toString())
  }

  install(SSE) {
    maxReconnectionAttempts = retry.attempts
    reconnectionTime = timeout.connection
  }

  install(WebSockets) { pingInterval = timeout.read }

  expectSuccess = true

  // install(SaveBodyPlugin) {
  //   disabled = true
  // }

  HttpResponseValidator {
    handleResponseExceptionWithRequest { ex, req ->
      val resException = ex as? ResponseException ?: return@handleResponseExceptionWithRequest
      val res = resException.response
      kLogger.trace { "Request failed: ${req.method.value} ${req.url} -> ${res.status}" }
    }
  }
}
