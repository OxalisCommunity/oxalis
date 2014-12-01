package eu.peppol.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder of global state to be used in the application
 *
 * @author thore
 */
public enum GlobalState {

    INSTANCE;

    private final Logger log = LoggerFactory.getLogger(GlobalState.class);
    private boolean hasBeenInitialized = false;

    private boolean transmissionBuilderOverride = false;

    public static GlobalState getInstance() {
        INSTANCE.initialize();
        return INSTANCE;
    }

    private GlobalState() {
        /* not allowed, use getInstance() */
    }

    private void initialize() {
        if (hasBeenInitialized) return;
        // determine if transmissions builder should be overridable
        transmissionBuilderOverride = OperationalMode.TEST.equals(GlobalConfiguration.getInstance().getModeOfOperation());
        if ("trUe".equalsIgnoreCase(System.getenv("oxalis.transmissionbuilder.override"))) {
            log.warn("Running with transmissionBuilderOverride enabled since ENVIRONMENT variable oxalis.transmissionbuilder.override=TRUE");
            transmissionBuilderOverride = true;
        }
        hasBeenInitialized = true;
    }

    public boolean isTransmissionBuilderOverride() {
        return transmissionBuilderOverride;
    }

    /**
     * This is here to assist UNIT tests only, and should NOT be used in production.
     * Makes it possible to override in runtime as well as using environment variable
     */
    public void setTransmissionBuilderOverride(boolean transmissionBuilderOverride) {
        this.transmissionBuilderOverride = transmissionBuilderOverride;
    }

}
