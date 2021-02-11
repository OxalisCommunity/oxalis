package network.oxalis.commons.error;

import network.oxalis.api.settings.DefaultValue;
import network.oxalis.api.settings.Path;
import network.oxalis.api.settings.Title;

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
