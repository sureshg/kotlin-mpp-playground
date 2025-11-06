package dev.suresh.plugins

import dev.suresh.plugins.custom.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.opentelemetry.api.*
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.ktor.v3_0.*
import kotlin.io.path.*
import kotlin.time.Clock

/**
 * Configures OpenTelemetry for the application. Checks if the OTel Java agent is active and
 * installs the custom OTel extension plugin. The agent status is logged for monitoring purposes.
 */
fun Application.configureOTel() {
  val globalOtel = GlobalOpenTelemetry.get()
  val isAgentActive = globalOtel !== OpenTelemetry.noop()
  when (isAgentActive) {
    true -> log.info("\uD83D\uDFE2 OTel Java agent is active")
    else -> log.warn("\uD83D\uDFE1 OTel Java agent is inactive")
  }

  install(OTelExtnPlugin)
  // otelSdk.shutdown().join(10, TimeUnit.SECONDS);
}

/**
 * Configures OpenTelemetry instrumentation for Ktor server.
 *
 * Important: When using Ktor plugins for instrumentation, disable the Java agent's Ktor
 * instrumentation by setting the environment variable: `OTEL_INSTRUMENTATION_KTOR_ENABLED=false`
 */
fun Application.otelInstrumentation() {
  install(KtorServerTelemetry) {
    setOpenTelemetry(GlobalOpenTelemetry.get())
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
}

/**
 * Declarative OpenTelemetry SDK configuration. Loads configuration from either:
 * - Environment variable OTEL_CONFIG_FILE if specified
 * - Default configuration from resources/otel/sdk-config.yaml
 *
 * For detailed configuration options, see:
 * [Configure the SDK](https://opentelemetry.io/docs/languages/java/configuration/)
 */
val dclOpenTelemetrySdk: OpenTelemetry by lazy {
  val sdkConfPath = System.getenv("OTEL_CONFIG_FILE")?.takeIf { it.isNotBlank() }?.let { Path(it) }
  val ins =
      when (sdkConfPath) {
        null -> object {}::class.java.getResourceAsStream("/otel/sdk-config.yaml")
        else -> sdkConfPath.inputStream()
      }
  TODO("FileConfiguration.parseAndCreate(ins)")
}
