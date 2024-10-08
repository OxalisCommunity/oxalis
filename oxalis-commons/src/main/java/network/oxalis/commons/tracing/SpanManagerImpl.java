package network.oxalis.commons.tracing;

import com.google.inject.Inject;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import network.oxalis.commons.util.ClosableSpan;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpanManagerImpl implements SpanManager {

    @Inject
    private Tracer tracer;

    public <T> T runWithinSpan(String spanName, Function<Span, T> function) {
        if (spanName == null) {
            throw new IllegalStateException("spanName needs to be set");
        }
        Span span = tracer.spanBuilder(spanName).startSpan();
        try {
            return function.apply(span);
        } finally {
            span.end();
        }
    }

    public ClosableSpan startClosableSpan(String spanName) {
       return tracer.spanBuilder(spanName).startSpan()::end;
    }

}
