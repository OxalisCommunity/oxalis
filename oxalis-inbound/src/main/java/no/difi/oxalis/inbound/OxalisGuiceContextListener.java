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

package no.difi.oxalis.inbound;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import no.difi.oxalis.api.config.GlobalConfiguration;
import no.difi.oxalis.commons.logback.LoggingConfigurator;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;

import javax.servlet.ServletContextEvent;

/**
 * Wires our object graph together using Google Guice.
 *
 * @author steinar
 *         Date: 29.11.13
 *         Time: 10:26
 * @author erlend
 */
public class OxalisGuiceContextListener extends GuiceServletContextListener {

    private Injector injector;

    public OxalisGuiceContextListener() {
        /*
        this(Guice.createInjector(
                // Mode
                new ModeModule(),

                // Tracing
                new TracingModule(),

                // Timestamp
                new TimestampModule(),

                // Persisters
                new PersisterModule(),

                // Verifier
                new VerifierModule(),

                // Provides the DBMS Repositories
                new OxalisRepositoryModule(),

                // Statistics
                new StatisticsModule(),

                // Plugins
                new PluginModule(),

                // And the Data source
                new OxalisDataSourceModule(),

                // AS2
                new As2InboundModule(),

                // SevletModule is provided by Guice
                new OxalisInboundModule()
        ));
        */
        this(GuiceModuleLoader.initiate());
    }

    public OxalisGuiceContextListener(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);

        // Configuration of logging.
        LoggingConfigurator loggingConfigurator =
                new LoggingConfigurator(injector.getInstance(GlobalConfiguration.class));
        loggingConfigurator.execute();
    }

    @Override
    public Injector getInjector() {
        return injector;
    }
}
