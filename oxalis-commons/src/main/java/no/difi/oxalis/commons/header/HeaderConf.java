package no.difi.oxalis.commons.header;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

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
