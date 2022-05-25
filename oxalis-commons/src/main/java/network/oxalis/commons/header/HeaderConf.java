package network.oxalis.commons.header;

import network.oxalis.api.settings.DefaultValue;
import network.oxalis.api.settings.Path;
import network.oxalis.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.2
 */
@Title("Header")
public enum HeaderConf {

    @Path("oxalis.header.parser")
    @DefaultValue("sbdh")
    PARSER
}
