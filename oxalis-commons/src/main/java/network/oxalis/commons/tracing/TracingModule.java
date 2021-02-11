/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

package network.oxalis.commons.tracing;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.ImplLoader;
import network.oxalis.commons.guice.OxalisModule;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * <p>
 * Available reports (brave.reporter):
 * <ul>
 * <li>console</li>
 * <li>http</li>
 * <li>slf4j</li>
 * </ul>
 *
 * @author erlend
 * @since 4.0.0
 */
public class TracingModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(BraveConf.class);
        bindSettings(TracingConf.class);

        bind(Key.get(Tracer.class, Names.named("noop")))
                .toProvider(NoopTracerFactory::create);
        bind(Key.get(Tracer.class, Names.named("brave")))
                .toProvider(BraveTracerProvider.class);

        bind(Key.get(Reporter.class, Names.named("console")))
                .toProvider(() -> Reporter.CONSOLE);
        bindTyped(Reporter.class, Slf4jReporter.class);
    }

    @Provides
    @Singleton
    @Named("http")
    protected Reporter getHttpReporter(Settings<BraveConf> settings) {
        return AsyncReporter
                .builder(URLConnectionSender.create(settings.getString(BraveConf.HTTP)))
                .build();
    }

    @Provides
    @Singleton
    protected Reporter getReporter(Injector injector, Settings<BraveConf> settings) {
        return ImplLoader.get(injector, Reporter.class, settings, BraveConf.REPORTER);
    }

    @Provides
    @Singleton
    protected Tracer getTracer(Injector injector, Settings<TracingConf> settings) {
        return ImplLoader.get(injector, Tracer.class, settings, TracingConf.TRACER);
    }
}
