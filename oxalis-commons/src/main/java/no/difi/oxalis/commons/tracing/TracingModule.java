/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.commons.tracing;

import brave.Tracer;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.TracerAdapter;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import no.difi.vefa.peppol.mode.Mode;
import zipkin.Endpoint;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

/**
 * <p>
 * Available reports (brave.reporter):
 * <ul>
 * <li>console</li>
 * <li>http</li>
 * <li>noop</li>
 * <li>slf4j</li>
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
    protected Reporter getHttpReporter(Config config) {
        return AsyncReporter
                .builder(URLConnectionSender.create(config.getString("brave.http")))
                .build();
    }

    @Provides
    @Singleton
    protected Reporter getReporter(Injector injector, Config config) {
        return injector.getInstance(Key.get(Reporter.class, Names.named(config.getString("brave.reporter"))));
    }

    @Provides
    @Singleton
    @SuppressWarnings("unchecked")
    protected Tracer getTracer(Reporter reporter) {
        return Tracer.newBuilder()
                .reporter(reporter)
                .traceId128Bit(true)
                .localEndpoint(Endpoint.create("Oxalis", 0))
                .build();
    }

    @Provides
    @Singleton
    protected Brave getBrave(Tracer tracer) {
        return TracerAdapter.newBrave(tracer);
    }
}
