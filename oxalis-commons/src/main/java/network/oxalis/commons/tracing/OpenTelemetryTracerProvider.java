package network.oxalis.commons.tracing;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

/**
 * @author erlend
 */
public class OpenTelemetryTracerProvider implements Provider<Tracer> {

    @Inject
    private OpenTelemetry openTelemetry;

    @Override
    public Tracer get() {
        return openTelemetry.getTracerProvider()
                .get("network.oxalis.commons.tracing.OpenTelemetryTracer");
    }

}
