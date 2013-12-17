package eu.peppol.outbound;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.peppol.outbound.transmission.TransmissionModule;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.Transmitter;

/**
 * Object factory for the Oxalis outbound module.
 *
 * Serves as the main entry point for external applications etc.
 *
 * @author steinar
 *         Date: 06.11.13
 *         Time: 22:59
 */
public class OxalisOutboundModule {

    private Injector injector;

    public OxalisOutboundModule() {
        injector = Guice.createInjector(new TransmissionModule());

        // TODO: Configure outbound logging

    }

    /**
     * Retrieves instances of TransmissionRequestBuilder, while not exposing Google Guice to the outside
     *
     * @return instance of TransmissionRequestBuilder
     */
    public TransmissionRequestBuilder getTransmissionRequestBuilder() {
        return injector.getInstance(TransmissionRequestBuilder.class);
    }

    /**
     * Retrieves instance of Transmitter, without revealing intern object dependency injection.
     *
     * @return instance of Transmitter
     */
    public Transmitter getTransmitter() {
        return injector.getInstance(Transmitter.class);
    }
}