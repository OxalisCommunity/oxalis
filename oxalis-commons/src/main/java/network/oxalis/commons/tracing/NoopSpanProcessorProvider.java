package network.oxalis.commons.tracing;

import com.google.inject.Provider;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class NoopSpanProcessorProvider implements Provider<SpanProcessor> {

    @Override
    public SpanProcessor get() {
        return new SpanProcessor() {
            @Override
            public void onStart(Context parentContext, ReadWriteSpan span) {
            }

            @Override
            public boolean isStartRequired() {
                return false;
            }

            @Override
            public void onEnd(ReadableSpan span) {
            }

            @Override
            public boolean isEndRequired() {
                return false;
            }

            @Override
            public String toString() {
                return "NoopSpanProcessor{}";
            }
        };
    }

}
