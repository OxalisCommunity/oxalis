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

package network.oxalis.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.GuiceModuleLoader;
import network.oxalis.inbound.OxalisGuiceContextListener;
import network.oxalis.server.jetty.JettyConf;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * @author erlend
 */
@Slf4j
@Singleton
public class Main {

    @Inject
    private Injector injector;

    @Inject
    private Settings<JettyConf> settings;

    public static void main(String... args) throws Exception {
        GuiceModuleLoader.initiate().getInstance(Main.class).run();
    }

    public void run() throws Exception {
        Server server = new Server(settings.getInt(JettyConf.PORT));

        HandlerList handlers = new HandlerList();

        if (settings.getString(JettyConf.SHUTDOWN_TOKEN) != null)
            handlers.addHandler(new ShutdownHandler(settings.getString(JettyConf.SHUTDOWN_TOKEN), false, true));

        ServletContextHandler handler = new ServletContextHandler(server, settings.getString(JettyConf.CONTEXT_PATH));
        handler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        handler.addEventListener(new OxalisGuiceContextListener(injector));
        handler.addServlet(DefaultServlet.class, "/");
        handlers.addHandler(handler);

        StatisticsHandler statisticsHandler = new StatisticsHandler();
        statisticsHandler.setHandler(handler);
        handlers.addHandler(statisticsHandler);

        server.setHandler(handlers);
        server.setStopTimeout(settings.getInt(JettyConf.STOP_TIMEOUT));
        server.setStopAtShutdown(true);

        log.info("Starting server");
        server.start();
        server.join();
    }
}
