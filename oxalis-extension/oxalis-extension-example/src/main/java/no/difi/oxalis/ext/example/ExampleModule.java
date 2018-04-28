package no.difi.oxalis.ext.example;

import no.difi.oxalis.api.transmission.TransmissionVerifier;
import no.difi.oxalis.commons.guice.OxalisModule;
import org.slf4j.LoggerFactory;

/**
 * Guice module where we bind our implementations.
 *
 * @author erlend
 * @since 4.0.1
 */
public class ExampleModule extends OxalisModule {

    public ExampleModule() {
        LoggerFactory.getLogger(ExampleModule.class)
                .info("Loaded.");
    }

    @Override
    protected void configure() {
        bindTyped(TransmissionVerifier.class, LoggingTransmissionVerifier.class);
    }
}
