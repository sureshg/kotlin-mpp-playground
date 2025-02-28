package dev.suresh.plugins.custom

import dev.suresh.plugins.isApi
import io.ktor.server.application.*
import io.ktor.util.AttributeKey
import io.opentelemetry.api.trace.Span

/**
 * A custom ktor plugin to automatically add OpenTelemetry trace id to response headers for API
 * endpoints.
 */
val OTelExtnPlugin =
    createApplicationPlugin(name = "OTelExtnPlugin", createConfiguration = ::OTelExtnPluginConfig) {
      val onCallTimeKey = AttributeKey<Long>("onCallTimeKey")
      onCall { call ->
        val onCallTime = System.currentTimeMillis()
        call.attributes.put(onCallTimeKey, onCallTime)

        if (pluginConfig.enabled && call.isApi) {
          Span.current()?.let { span ->
            call.response.headers.append(pluginConfig.traceIdHeader, span.spanContext.traceId)
            // span.setAttribute("custom-attribute", "TestPlugin")
            // span.addEvent("TestPluginEvent")
          }
        }
      }

      //  on(MonitoringEvent(ApplicationStarted)) {
      //    it.log.info("Application started - ${it.isActive}")
      //  }
      //
      //  on(CallFailed) { call, error ->
      //    if (pluginConfig.enabled) {
      //      call.application.log.error("Failed call: $call", error)
      //    }
      //  }
      //
      //  onCallReceive { call, body ->
      //    val onCallTime = call.attributes[onCallTimeKey]
      //    if (pluginConfig.enabled) {
      //      call.application.log.info("Received: $body")
      //    }
      //  }
      //
      //  onCallRespond { call, res ->
      //    if (pluginConfig.enabled) {
      //      call.application.log.info("Responded: $res")
      //    }
      //  }
    }

class OTelExtnPluginConfig {
  var enabled: Boolean = false
  var traceIdHeader: String = "X-Trace-Id"
}
