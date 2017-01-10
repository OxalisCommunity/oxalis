package eu.peppol.outbound.module;

import brave.Tracer;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import eu.peppol.outbound.util.Slf4jReporter;
import no.difi.vefa.peppol.mode.Mode;
import zipkin.Span;
import zipkin.reporter.Reporter;

public class BraveTraceModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    @Named("slf4j")
    public Reporter getReporter() {
        return new Slf4jReporter();
    }

    @Provides
    @Singleton
    @Named("noop")
    public Reporter getNoopReporter() {
        return Reporter.NOOP;
    }

    @Provides
    @Singleton
    @SuppressWarnings("unchecked")
    public Tracer getTracer(Injector injector, Mode mode) {
        return Tracer.newBuilder()
                .reporter((Reporter<Span>) injector
                        .getProvider(Key.get(Reporter.class, Names.named(mode.getString("brave.reporter")))).get())
                .traceId128Bit(true)
                .build();
    }
}
