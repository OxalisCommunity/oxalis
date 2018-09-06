package no.difi.oxalis.ext.testbed.v1;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.3
 */
@Title("Testbed")
public enum TestbedConf {

    @Path("oxalis.testbed.v1.password")
    @DefaultValue("testbed")
    PASSWORD,

    @Path("oxalis.testbed.v1.controller")
    @DefaultValue("https://localhost/controller")
    CONTROLLER,

    @Path("oxalis.testbed.v1.path")
    @DefaultValue("testbed")
    PATH,

}
