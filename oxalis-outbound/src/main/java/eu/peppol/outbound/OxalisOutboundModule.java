package eu.peppol.outbound;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.peppol.outbound.transmission.TransmissionModule;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.Transmitter;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpModule;

/**
 * Object factory for the Oxalis outbound module.
 *
 * Google guice is very lightweight, so there is really no need to make this a singleton in order to optimize for performance.
 *
 * @author steinar
 * @author thore
 */
public class OxalisOutboundModule {

    Injector injector;

    public OxalisOutboundModule() {
        injector = Guice.createInjector(
                new SmpModule(),
                new TransmissionModule()
        );
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

    /**
     * Retrieves instance of SmpLookupManager, without revealing intern object dependency injection.
     */
    public SmpLookupManager getSmpLookupManager() {
        return injector.getInstance(SmpLookupManager.class);
    }

}