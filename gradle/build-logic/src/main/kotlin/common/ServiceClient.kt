package common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Resource("/users/{name}") class UserReq(val name: String)

@Serializable data class User(val id: Long, val login: String, val name: String)

object ApiClient {
  fun get() =
      HttpClient(Java) {
        install(Resources)
        install(ContentNegotiation) {
          json(
              Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
                decodeEnumsCaseInsensitive = true
                allowTrailingComma = true
              })
        }

        install(ContentEncoding) {
          deflate(1.0F)
          gzip(0.9F)
        }

        install(HttpRequestRetry) {
          maxRetries = 2
          retryOnException(retryOnTimeout = true)
          retryOnServerErrors()
          constantDelay(millis = 1_000)
        }

        install(HttpTimeout) {
          connectTimeoutMillis = 10_000
          requestTimeoutMillis = 5_000
          socketTimeoutMillis = 5_000
        }

        install(HttpCookies)

        install(Logging) {
          logger = Logger.DEFAULT
          level = LogLevel.ALL
          sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        engine { pipelining = true }

        followRedirects = true

        install(UserAgent) { agent = "Service Client" }

        install(DefaultRequest) {
          url("https://api.github.com")
          headers.appendIfNameAndValueAbsent(
              HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }

        install(Auth) {
          basic {
            credentials {
              sendWithoutRequest { true }
              BasicAuthCredentials(username = "", password = "")
            }
          }
        }

        // expectSuccess = false
        HttpResponseValidator {
          validateResponse {
            when (it.status.value) {
              in 300..399 -> throw RedirectResponseException(it, "Redirect error")
              in 400..499 -> throw ClientRequestException(it, "Client error")
              in 500..599 -> throw ServerResponseException(it, "Server error")
            }
          }
        }
      }

  suspend fun user(name: String) = get().get(UserReq(name)).body<User>()
}
