package dev.suresh

import dev.suresh.http.json
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CommonTest {

  @Test
  fun greetings() {
    assertTrue(
        actual = Greeting().greeting().contains("Kotlin"), message = "Check 'Kotlin' is mentioned")
  }

  @Test
  fun testHttpClient() = runTest {
    val mockEngine = MockEngine { req ->
      respondError(HttpStatusCode.BadRequest, "Client Error Response")
    }

    val client =
        HttpClient(mockEngine) {
          install(Resources)
          install(ContentNegotiation) { json(json) }
          install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
          }
          install(HttpTimeout) {
            requestTimeoutMillis = 5_000
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 5_000
          }
          install(HttpCookies)
          install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
          }
          engine { pipelining = true }
          followRedirects = true
          defaultRequest {
            headers.appendIfNameAndValueAbsent(
                HttpHeaders.ContentType, ContentType.Application.Json.toString())
          }

          HttpResponseValidator {
            validateResponse {
              // it.body<String>()
              when (it.status.value) {
                in 300..399 -> throw RedirectResponseException(it, "Redirect error")
                in 400..499 -> throw ClientRequestException(it, "Client error")
                in 500..599 -> throw ServerResponseException(it, "Server error")
              }
            }
          }
        }

    try {
      client.get("/")
    } catch (e: ClientRequestException) {
      assertEquals(
          expected = "Client Error Response",
          actual = e.response.bodyAsText(),
          message = "Check response body")
    }
  }
}
