package no.difi.oxalis.commons.tracing;

import brave.Tracer;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import no.difi.vefa.peppol.mode.Mode;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

/**
 * <p>
 * Available reports (brave.reporter):
 * <ul>
 *  <li>console</li>
 *  <li>http</li>
 *  <li>noop</li>
 *  <li>slf4j</li>
 * </ul>
 *
 * @author erlend
 * @since 4.0.0
 */
public class TracingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(Reporter.class, Names.named("console")))
                .toInstance(Reporter.CONSOLE);

        bind(Key.get(Reporter.class, Names.named("noop")))
                .toInstance(Reporter.NOOP);

        bind(Key.get(Reporter.class, Names.named("slf4j")))
                .to(Slf4jReporter.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    @Named("http")
    public Reporter getHttpReporter(Mode mode) {
        return AsyncReporter
                .builder(URLConnectionSender.create(mode.getString("brave.http")))
                .build();
    }

    @Provides
    @Singleton
    @SuppressWarnings("unchecked")
    public Tracer getTracer(Injector injector, Mode mode) {
        return Tracer.newBuilder()
                .reporter((Reporter<Span>) injector
                        .getProvider(Key.get(Reporter.class, Names.named(mode.getString("brave.reporter")))).get())
                .traceId128Bit(true)
                .localEndpoint(Endpoint.create("Oxalis", 0))
                .build();
    }
}
