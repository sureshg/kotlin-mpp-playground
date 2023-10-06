package dev.suresh.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureSecurity() {
  authentication {
    bearer("auth-bearer") {
      realm = "Ktor App"
      authenticate { tokenCredential ->
        when (tokenCredential.token) {
          "token" -> UserIdPrincipal("admin")
          else -> null
        }
      }
    }

    basic("admin") {
      realm = "App Admin"
      validate { credentials ->
        when (credentials.name == "admin" && credentials.password == "admin") {
          true -> UserIdPrincipal(credentials.name)
          else -> null
        }
      }
    }
  }
}
