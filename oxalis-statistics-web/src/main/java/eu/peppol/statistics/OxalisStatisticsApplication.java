package eu.peppol.statistics;

import com.sun.jersey.api.core.PackagesResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * @author steinar
 *         Date: 15.04.13
 *         Time: 17:22
 */
// TODO: delete this class.
//@ApplicationPath("resource")
public class OxalisStatisticsApplication extends PackagesResourceConfig {

    public OxalisStatisticsApplication() {
        super("eu.peppol.statistics.resource");
    }
}
