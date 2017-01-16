package eu.peppol.outbound.transmission;

import brave.Span;
import brave.Tracer;
import com.google.inject.Inject;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.TransmissionService;
import no.difi.oxalis.api.outbound.Transmitter;

import java.io.IOException;
import java.io.InputStream;

/**
 * Default implementation of {@link TransmissionService}.
 *
 * @author erlend
 */
class DefaultTransmissionService implements TransmissionService {

    private TransmissionRequestFactory transmissionRequestFactory;

    private Transmitter transmitter;

    private Tracer tracer;

    /**
     * {@inheritDoc}
     */
    @Inject
    public DefaultTransmissionService(TransmissionRequestFactory transmissionRequestFactory,
                                      Transmitter transmitter, Tracer tracer) {
        this.transmissionRequestFactory = transmissionRequestFactory;
        this.transmitter = transmitter;
        this.tracer = tracer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransmissionResponse send(InputStream inputStream) throws IOException, OxalisTransmissionException {
        try (Span root = tracer.newTrace().name("TransmissionService").start()) {
            return send(inputStream, root);
        }
    }

    @Override
    public TransmissionResponse send(InputStream inputStream, Span root)
            throws IOException, OxalisTransmissionException {
        return transmitter.transmit(transmissionRequestFactory.newInstance(inputStream, root), root);
    }
}
