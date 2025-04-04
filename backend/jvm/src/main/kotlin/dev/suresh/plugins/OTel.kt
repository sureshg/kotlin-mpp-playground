package dev.suresh.plugins

import BuildConfig
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.request.httpMethod
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.ktor.v3_0.*
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.semconv.ServiceAttributes
import kotlin.time.Clock

fun Application.configureOTel() {
  install(KtorServerTelemetry) {
    setOpenTelemetry(getOpenTelemetry(BuildConfig.name))

    spanKindExtractor {
      if (httpMethod == HttpMethod.Post) {
        SpanKind.PRODUCER
      } else {
        SpanKind.CLIENT
      }
    }

    attributesExtractor {
      onStart { attributes.put("start-time", Clock.System.now().toEpochMilliseconds()) }
      onEnd { attributes.put("end-time", Clock.System.now().toEpochMilliseconds()) }
    }
  }
  // install(OTelExtnPlugin) { enabled = true }
}

/** See [Configure the SDK](https://opentelemetry.io/docs/languages/java/configuration/) */
fun getOpenTelemetry(serviceName: String): OpenTelemetrySdk =
    AutoConfiguredOpenTelemetrySdk.builder()
        .addResourceCustomizer { res, _ ->
          res.toBuilder()
              .putAll(res.attributes)
              .put(ServiceAttributes.SERVICE_NAME, serviceName)
              .build()
        }
        .build()
        .openTelemetrySdk
