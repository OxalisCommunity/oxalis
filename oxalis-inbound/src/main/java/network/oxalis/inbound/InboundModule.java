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

package network.oxalis.inbound;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import network.oxalis.api.inbound.InboundService;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.ImplLoader;
import network.oxalis.commons.settings.SettingsBuilder;
import network.oxalis.inbound.servlet.HomeServlet;
import network.oxalis.inbound.servlet.StatusServlet;
import network.oxalis.inbound.tracing.DefaultOpenTelemetryTracingFilter;
import network.oxalis.inbound.tracing.OpenTelemetryServletConf;
import network.oxalis.inbound.tracing.OpenTelemetryTracingFilter;

/**
 * @author erlend
 */
public class InboundModule extends ServletModule {

    @Override
    protected void configureServlets() {
        SettingsBuilder.with(binder(), OpenTelemetryServletConf.class);

        filter("/*").through(OpenTelemetryTracingFilter.class);

        serve("/").with(HomeServlet.class);
        serve("/status").with(StatusServlet.class);

        bind(InboundService.class).to(DefaultInboundService.class);

        bind(Key.get(OpenTelemetryTracingFilter.class, Names.named("default")))
                .to(DefaultOpenTelemetryTracingFilter.class);
    }

    @Provides
    @Singleton
    protected OpenTelemetryTracingFilter getOpenTelemetryTracingFilter(Injector injector, Settings<OpenTelemetryServletConf> settings) {
        return ImplLoader.get(injector, OpenTelemetryTracingFilter.class, settings, OpenTelemetryServletConf.TRACING_FILTER);
    }

}
