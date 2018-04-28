package no.difi.oxalis.commons.transformer;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.1
 */
@Title("Transformer")
public enum TransformerConf {

    @Path("oxalis.transformer.detector")
    @DefaultValue("legacy")
    DETECTOR,

    @Path("oxalis.transformer.wrapper")
    @DefaultValue("xml")
    WRAPPER

}
