package dev.suresh.http

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin

class CurlLoggingConfig {
  var enabled: Boolean = true
  var logger: KLogger? = null
}

val CurlLogging: ClientPlugin<CurlLoggingConfig> =
    createClientPlugin(name = "CurlLogging", createConfiguration = ::CurlLoggingConfig) {
      if (pluginConfig.enabled) {
        onRequest { req, _ ->
          // pluginConfig.logger.is
        }
      }
    }
