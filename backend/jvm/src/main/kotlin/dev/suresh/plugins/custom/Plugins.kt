package dev.suresh.plugins.custom

import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import kotlinx.coroutines.isActive

val TestPlugin =
    createApplicationPlugin(name = "TestPlugin", createConfiguration = ::TestPluginConfig) {
      on(MonitoringEvent(ApplicationStarted)) {
        it.log.info("[TestPlugin] Application started - ${it.isActive}")
      }

      on(CallFailed) { call, error ->
        if (pluginConfig.enabled) {
          call.application.log.error("[TestPlugin]Failed call: $call", error)
        }
      }

      onCall {
        if (pluginConfig.enabled) {
          it.response.headers.append("X-Custom-Header", "TestPlugin")
        }
      }

      onCallReceive { call, body ->
        if (pluginConfig.enabled) {
          call.application.log.info("[TestPlugin] Received: $body")
        }
      }

      onCallRespond { call, res ->
        if (pluginConfig.enabled) {
          call.application.log.info("[TestPlugin] Responded: $res")
        }
      }
    }

class TestPluginConfig {
  var enabled: Boolean = false
}

fun Application.customPlugins() {
  install(TestPlugin) { enabled = false }
}
