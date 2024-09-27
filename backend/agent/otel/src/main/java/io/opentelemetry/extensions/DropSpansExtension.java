package io.opentelemetry.extensions;

import com.google.auto.service.AutoService;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.UrlAttributes;


@AutoService(AutoConfigurationCustomizerProvider.class)
public class DropSpansExtension implements AutoConfigurationCustomizerProvider {

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        // Set the sampler to be the default parentbased_always_on, but drop calls listed in the env variable
        final var dropSpansEnv = System.getenv("OTEL_DROP_SPANS");
        if (dropSpansEnv != null) {
            final var dropSpanBuilder = RuleBasedRoutingSampler.builder(SpanKind.SERVER, Sampler.parentBased(Sampler.alwaysOn()));
            for (var span : dropSpansEnv.split(",")) {
                dropSpanBuilder.drop(UrlAttributes.URL_PATH, span);
            }

            autoConfiguration.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) -> sdkTracerProviderBuilder.setSampler(Sampler.parentBased(dropSpanBuilder.build())));
        }
    }
}