package dev.suresh.plugins

import dev.suresh.plugins.custom.OTelExtnPlugin
import io.ktor.server.application.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.semconv.ServiceAttributes

fun Application.configureOTel() {
  install(OTelExtnPlugin) { enabled = true }

  //  install(KtorServerTracing) {
  //    setOpenTelemetry(GlobalOpenTelemetry.get())
  //    attributeExtractor {
  //      onStart { attributes.put("start-time", Clock.System.now().toEpochMilliseconds()) }
  //      onEnd { attributes.put("end-time", Clock.System.now().toEpochMilliseconds()) }
  //    }
  //  }
}

/**
 * [Manual
 * instrumentation](https://opentelemetry.io/docs/languages/java/instrumentation/#acquiring-a-tracer-in-java-agent)
 */
fun getOpenTelemetry(serviceName: String): OpenTelemetry {
  return AutoConfiguredOpenTelemetrySdk.builder()
      .addResourceCustomizer { oldResource, _ ->
        oldResource
            .toBuilder()
            .putAll(oldResource.attributes)
            .put(ServiceAttributes.SERVICE_NAME, serviceName)
            .build()
      }
      .build()
      .openTelemetrySdk
}
