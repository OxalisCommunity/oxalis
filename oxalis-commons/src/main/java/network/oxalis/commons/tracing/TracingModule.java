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
import com.google.inject.name.Names;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.SpanProcessor;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.ImplLoader;
import network.oxalis.commons.guice.OxalisModule;
import network.oxalis.commons.util.OpenTelemetryUtils;

/**
 * <p>
 * Available span processors (oxalis.tracing.open-telemetry.span-processor):
 * <ul>
 * <li>noop(default)</li>
 * <li>slf4j</li>
 * </ul>
 *
 * @author erlend
 * @since 4.0.0
 */
public class TracingModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(OpenTelemetryConf.class);

        bind(Key.get(SpanProcessor.class, Names.named("noop")))
                .toProvider(NoopSpanProcessorProvider.class);
        bind(Key.get(SpanProcessor.class, Names.named("slf4j")))
                .to(Slf4jSpanProcessor.class);

        bind(Key.get(Tracer.class))
                .toProvider(OpenTelemetryTracerProvider.class);

        bind(Key.get(SpanManager.class))
                .to(SpanManagerImpl.class);
    }

    @Provides
    @Singleton
    protected SpanProcessor getSpanProcessor(Injector injector, Settings<OpenTelemetryConf> settings) {
        return ImplLoader.get(injector, SpanProcessor.class, settings, OpenTelemetryConf.SPAN_PROCESSOR);
    }

    @Provides
    @Singleton
    protected OpenTelemetry openTelemetry(SpanProcessor spanProcessor) {
        return OpenTelemetryUtils.initOpenTelemetry(spanProcessor);
    }

}
