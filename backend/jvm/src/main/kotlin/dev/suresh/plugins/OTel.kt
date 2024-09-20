package dev.suresh.plugins

import com.google.auto.service.AutoService
import io.ktor.server.application.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.javaagent.bootstrap.http.*
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.semconv.ServiceAttributes

fun Application.configureOTel() {
  //  install(KtorServerTracing) {
  //    setOpenTelemetry(GlobalOpenTelemetry.get())
  //    attributeExtractor {
  //      onStart { attributes.put("start-time", Clock.System.now().toEpochMilliseconds()) }
  //      onEnd { attributes.put("end-time", Clock.System.now().toEpochMilliseconds()) }
  //    }
  //  }
}

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

@AutoService(HttpServerResponseCustomizer::class)
class TraceIDResponseCustomizer : HttpServerResponseCustomizer {
  override fun <T> customize(
      serverContext: Context,
      response: T,
      responseMutator: HttpServerResponseMutator<T>
  ) {
    val spanContext = Span.fromContextOrNull(serverContext)?.spanContext
    if (spanContext?.isValid != true) return
    responseMutator.appendHeader(response, "X-Trace-Id", spanContext.traceId)
  }
}
