package dev.suresh.plugins.custom

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

class AuthzPluginConfig {
  var enabled: Boolean = false
  var roles: Set<String> = emptySet()
  lateinit var getRole: (userName: String?) -> String
}

val AuthzPlugin =
    createRouteScopedPlugin(name = "AuthzPlugin", createConfiguration = ::AuthzPluginConfig) {
      if (!pluginConfig.enabled) return@createRouteScopedPlugin
      application.log.info("\uD83E\uDDE9 AuthzPlugin is installed")

      on(AuthenticationChecked) { call ->
        val userName = call.principal<UserIdPrincipal>()?.name
        val userRole = pluginConfig.getRole(userName)
        if (userRole !in pluginConfig.roles) {
          call.respondText(
              "You are not allowed to visit this page",
              status = Forbidden,
          )
        }
      }
    }
