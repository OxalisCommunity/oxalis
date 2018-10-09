package no.difi.oxalis.commons.executor;

import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.oxalis.api.settings.Title;

/**
 * @author erlend
 */
@Title("Executor")
public enum ExecutorConf {

    @Path("oxalis.executor.threads")
    @DefaultValue("50")
    THREADS

}
