package common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
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
            host = "api.github.com"
          }
        }
        engine { pipelining = true }
      }

  suspend fun user(name: String) = get().get(UserReq(name)).body<User>()
}
