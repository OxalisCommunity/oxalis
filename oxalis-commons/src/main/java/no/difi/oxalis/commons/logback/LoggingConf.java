package no.difi.oxalis.commons.logback;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 */
@Title("Logging")
public enum LoggingConf {

    @Path("oxalis.logging.config")
    @DefaultValue("logback.xml")
    CONFIG

}
