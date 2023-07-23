package common

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Resource("json") class JsonResource()

object ServiceClient {
  fun get() =
      HttpClient(Java) {
        install(Resources)
        install(ContentNegotiation) {
          json(
              Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
              })
        }
        install(HttpTimeout) {
          requestTimeoutMillis = 20_000
          connectTimeoutMillis = 5_000
          socketTimeoutMillis = 5_000
        }
        defaultRequest {
          url {
            protocol = URLProtocol.HTTPS
            host = "app.dev"
          }
        }
        engine { pipelining = true }
      }
}
