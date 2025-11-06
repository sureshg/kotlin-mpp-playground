package dev.suresh.plugins.custom

import dev.suresh.plugins.isApi
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.*
import io.opentelemetry.api.trace.Span
import kotlinx.serialization.Serializable

@Serializable
data class OTelExtnPluginConfig(
    val enabled: Boolean = false,
    val traceIdHeader: String = "X-TraceId",
)

/**
 * A custom ktor plugin to automatically add OpenTelemetry trace id to response headers for API
 * endpoints.
 */
val OTelExtnPlugin =
    createApplicationPlugin(name = "OTelExtnPlugin") {
      val config: OTelExtnPluginConfig = application.property("plugins.otel-extn")
      if (!config.enabled) return@createApplicationPlugin
      application.log.info("\uD83E\uDDE9 OTelExtnPlugin is installed")

      val onCallTimeKey = AttributeKey<Long>("onCallTimeKey")
      onCall { call ->
        val onCallTime = System.currentTimeMillis()
        call.attributes.put(onCallTimeKey, onCallTime)
        if (call.isApi) {
          Span.current()?.let { span ->
            call.response.headers.append(config.traceIdHeader, span.spanContext.traceId)
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
