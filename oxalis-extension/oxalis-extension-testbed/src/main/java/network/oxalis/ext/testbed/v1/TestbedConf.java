package network.oxalis.ext.testbed.v1;

import network.oxalis.api.settings.DefaultValue;
import network.oxalis.api.settings.Path;
import network.oxalis.api.settings.Title;

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

}
