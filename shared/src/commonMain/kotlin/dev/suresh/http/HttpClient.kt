package dev.suresh.http

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
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
    decodeEnumsCaseInsensitive = true
    allowTrailingComma = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    allowComments = true
    classDiscriminatorMode = POLYMORPHIC
  }
}

/** Multiplatform HTTP client factory function. */
expect fun httpClient(
    name: String = "Api Client",
    timeout: Timeout = Timeout.DEFAULT,
    retry: Retry = Retry.DEFAULT,
    config: HttpClientConfig<*>.() -> Unit = {
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
      }

      install(HttpTimeout) {
        connectTimeoutMillis = timeout.connection.inWholeMilliseconds
        requestTimeoutMillis = timeout.read.inWholeMilliseconds
        socketTimeoutMillis = timeout.write.inWholeMilliseconds
      }

      install(HttpCookies)

      install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.INFO
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
      }

      engine { pipelining = true }

      followRedirects = true

      install(UserAgent) { agent = name }

      install(DefaultRequest) {
        headers.appendIfNameAndValueAbsent(
            HttpHeaders.ContentType, ContentType.Application.Json.toString())
      }

      // install(Auth) {
      //   basic {
      //     credentials {
      //       sendWithoutRequest { true }
      //       BasicAuthCredentials(username = "", password = "")
      //     }
      //   }
      // }
      //
      // expectSuccess = false
      //
      // HttpResponseValidator {
      //   validateResponse {
      //     when (it.status.value) {
      //       in 300..399 -> throw RedirectResponseException(it, "Redirect error")
      //       in 400..499 -> throw ClientRequestException(it, "Client error")
      //       in 500..599 -> throw ServerResponseException(it, "Server error")
      //     }
      //   }
      // }
    }
): HttpClient
