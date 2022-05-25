package network.oxalis.server.jetty;

import network.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 */
public class JettyModule extends OxalisModule {

    @Override
    protected void configure() {
        bindSettings(JettyConf.class);
    }
}
