package dev.suresh.plugins

import com.google.auto.service.AutoService
import io.ktor.server.application.Application
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.javaagent.bootstrap.http.HttpServerResponseCustomizer
import io.opentelemetry.javaagent.bootstrap.http.HttpServerResponseMutator

fun Application.configureOTel() {
  //  install(KtorServerTracing) {
  //    setOpenTelemetry(GlobalOpenTelemetry.get())
  //  }
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
