package dev.suresh.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.suresh.di.Auth
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.di.dependencies

fun Application.configureSecurity() {
  val auth: Auth by dependencies

  authentication {
    basic("admin") {
      realm = "App Admin"
      validate { credentials ->
        when (credentials.name == auth.admin.user && credentials.password == auth.admin.password) {
          true -> UserIdPrincipal(credentials.name)
          else -> null
        }
      }
    }

    bearer("auth-bearer") {
      realm = "Ktor App"
      authenticate { tokenCredential ->
        when (tokenCredential.token) {
          auth.api.bearerToken -> UserIdPrincipal(auth.api.user)
          else -> null
        }
      }
    }

    jwt("auth-jwt") {
      realm = "Ktor App"

      verifier {
        JWT.require(Algorithm.HMAC256(auth.api.bearerToken)).withIssuer(BuildConfig.name).build()
      }

      validate { cred -> cred.payload.subject?.let { JWTPrincipal(cred.payload) } }
      challenge { defaultScheme, realm ->
        call.respondError(Unauthorized, "Token is not valid or has expired")
      }
    }
  }
}
