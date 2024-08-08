package dev.suresh.http

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Resource("/media-api/images.json") class ImgRes

@Resource("/media-api/videos.json") class VideoRes

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

data class MediaApiClient(
    val timeout: Timeout = Timeout.DEFAULT,
    val retry: Retry = Retry.DEFAULT
) : AutoCloseable {

  private val log = KotlinLogging.logger {}

  private val client =
      httpClient(
              name = "Media API Client",
              timeout = timeout,
              retry = retry,
              httpLogger = log,
          )
          .config {
            defaultRequest { url("https://suresh.dev/") }

            // install(Auth) {
            //   basic {
            //     sendWithoutRequest { true }
            //     credentials { BasicAuthCredentials(username = "", password = "") }
            //   }
            // }
          }

  suspend fun images() = client.get(ImgRes()).body<List<Image>>()

  suspend fun videos() = client.get(VideoRes()).body<List<Video>>()

  override fun close() = client.close()
}
