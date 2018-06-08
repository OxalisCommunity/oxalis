package no.difi.oxalis.commons.error;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.2
 */
@Title("Error")
public enum ErrorConf {

    @Path("oxalis.error.handler")
    @DefaultValue("quiet")
    TRACKER,

}
