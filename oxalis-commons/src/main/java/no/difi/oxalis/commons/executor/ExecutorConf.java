package no.difi.oxalis.commons.executor;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 * @since 4.0.3
 */
@Title("Executor")
public enum ExecutorConf {

    @Path("oxalis.executor.default")
    @DefaultValue("50")
    DEFAULT,

    @Path("oxalis.executor.statistics")
    @DefaultValue("50")
    STATISTICS

}
