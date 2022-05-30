package network.oxalis.commons.tracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.opentracing.Tracer;
import zipkin2.reporter.Reporter;

/**
 * @author erlend
 */
public class BraveTracerProvider implements Provider<Tracer> {

    @Inject
    private Reporter reporter;

    @Override
    public Tracer get() {
        Tracing tracing = Tracing.newBuilder()
                .localServiceName("Oxalis")
                .spanReporter(reporter)
                .build();

        return BraveTracer.create(tracing);
    }
}
