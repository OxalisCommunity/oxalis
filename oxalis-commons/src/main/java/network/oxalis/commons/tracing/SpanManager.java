package network.oxalis.commons.tracing;

import io.opentelemetry.api.trace.Span;
import network.oxalis.commons.util.ClosableSpan;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface SpanManager {

    <T> T runWithinSpan(String spanName, Function<Span, T> function);

    default <T> T runWithinSpan(String spanName, Supplier<T> function) {
        return runWithinSpan(spanName, span -> {
            return function.get();
        });
    }

    default void runWithinSpan(String spanName, Consumer<Span> function) {
        runWithinSpan(spanName, span -> {
            function.accept(span);
            return null;
        });
    }

    ClosableSpan startClosableSpan(String spanName);

}
