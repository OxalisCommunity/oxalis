package no.difi.oxalis.server.jetty;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Nullable;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 */
@Title("Jetty")
public enum JettyConf {

    @Path("oxalis.jetty.port")
    @DefaultValue("8080")
    PORT,

    @Path("oxalis.jetty.context_path")
    @DefaultValue("/")
    CONTEXT_PATH,

    @Path("oxalis.jetty.shutdown_token")
    @Nullable
    SHUTDOWN_TOKEN,

    @Path("oxalis.jetty.stop_timeout")
    @DefaultValue("10000")
    STOP_TIMEOUT,

}
