package eu.peppol.outbound;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.peppol.outbound.transmission.TransmissionModule;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.Transmitter;

/**
 * @author steinar
 *         Date: 06.11.13
 *         Time: 22:59
 */
public class OxalisOutboundModule {

    private Injector injector;

    public OxalisOutboundModule() {
        injector = Guice.createInjector(new TransmissionModule());
    }


    public TransmissionRequestBuilder getTransmissionRequestBuilder() {
        return injector.getInstance(TransmissionRequestBuilder.class);
    }

    public Transmitter getTransmitter() {
        return injector.getInstance(Transmitter.class);
    }
}