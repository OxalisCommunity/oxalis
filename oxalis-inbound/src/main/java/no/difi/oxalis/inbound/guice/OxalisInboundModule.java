package no.difi.oxalis.inbound.guice;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.servlet.BraveServletFilter;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import eu.peppol.inbound.server.HomeServlet;
import eu.peppol.inbound.server.StatisticsServlet;
import eu.peppol.inbound.server.StatusServlet;

/**
 * @author erlend
 */
public class OxalisInboundModule extends ServletModule {

    @Override
    protected void configureServlets() {
        // Enable Zipkin tracing
        filterRegex("/*").through(BraveServletFilter.class);

        serve("/").with(HomeServlet.class);
        serve("/status").with(StatusServlet.class);
        serve("/statistics/*").with(StatisticsServlet.class);
    }

    @Provides
    @Singleton
    protected BraveServletFilter getBraveServletFilter(Brave brave) {
        return BraveServletFilter.create(brave);
    }
}
