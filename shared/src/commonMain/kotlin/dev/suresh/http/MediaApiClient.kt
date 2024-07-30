package dev.suresh.http

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
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

@Resource("/media-api/images.json") class ImgRes()

@Resource("/media-api/videos.json") class VideoRes()

@Serializable
data class Image(
    val category: String,
    val path: String,
    val author: String,
)

@Serializable
data class Video(
    val description: String,
    val sources: List<String>,
    val subtitle: String,
    val thumb: String,
    val title: String,
    val poster: String? = null,
)

class MediaApiClient(val timeout: Timeout = Timeout.DEFAULT, val retry: Retry = Retry.DEFAULT) {

  private val log = KotlinLogging.logger {}

  private val client =
      HttpClient {
        install(Resources)
        install(ContentNegotiation) { json(dev.suresh.json) }

        install(ContentEncoding) {
          deflate(1.0F)
          gzip(0.9F)
        }

        install(HttpRequestRetry) {
          maxRetries = retry.attempts
          retryOnException(retryOnTimeout = true)
          retryOnServerErrors()
          constantDelay(millis = retry.maxDelay.inWholeMilliseconds)
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

        install(UserAgent) { agent = "Image API Client" }

        install(DefaultRequest) {
          url("https://suresh.dev/")
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

  suspend fun images() = client.get(ImgRes()).body<List<Image>>()

  suspend fun videos() = client.get(VideoRes()).body<List<Video>>()

  fun close() = client.close()
}
