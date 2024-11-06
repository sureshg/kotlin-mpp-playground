package io.opentelemetry.extensions;

import com.google.auto.service.AutoService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.javaagent.bootstrap.http.HttpServerResponseCustomizer;
import io.opentelemetry.javaagent.bootstrap.http.HttpServerResponseMutator;

/**
 * Customizes the HTTP response by adding the trace ID as a header.
 */
@AutoService(HttpServerResponseCustomizer.class)
public class HttpResponseCustomizer implements HttpServerResponseCustomizer {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";


    @Override
    public <T> void customize(Context serverContext, T response, HttpServerResponseMutator<T> responseMutator) {
        var span = Span.fromContextOrNull(serverContext);
        if (span != null) {
            var spanContext = span.getSpanContext();
            if (spanContext.isValid() && spanContext.isSampled()) {
                responseMutator.appendHeader(response, TRACE_ID_HEADER, spanContext.getTraceId());
            }
        }
    }
}
