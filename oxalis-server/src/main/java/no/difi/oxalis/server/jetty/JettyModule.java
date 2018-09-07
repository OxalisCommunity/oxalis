package no.difi.oxalis.server.jetty;

import no.difi.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 */
public class JettyModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(JettyConf.class);
    }
}
