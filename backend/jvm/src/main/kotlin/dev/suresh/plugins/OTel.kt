package dev.suresh.plugins

import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.request.httpMethod
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.ktor.v3_0.*
import io.opentelemetry.sdk.extension.incubator.fileconfig.*
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.time.Clock

fun Application.configureOTel() {
  install(KtorServerTelemetry) {
    setOpenTelemetry(openTelemetrySdk)

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

  // openTelemetrySdk.shutdown().join(10, TimeUnit.SECONDS);
}

/** See [Configure the SDK](https://opentelemetry.io/docs/languages/java/configuration/) */
val openTelemetrySdk by lazy {
  val sdkConfPath =
      System.getenv("OTEL_EXPERIMENTAL_CONFIG_FILE")?.takeIf { it.isNotBlank() }?.let { Path(it) }
  val ins =
      when (sdkConfPath) {
        null -> object {}::class.java.getResourceAsStream("/otel/sdk-config.yaml")
        else -> sdkConfPath.inputStream()
      }
  DeclarativeConfiguration.parseAndCreate(ins)
}
