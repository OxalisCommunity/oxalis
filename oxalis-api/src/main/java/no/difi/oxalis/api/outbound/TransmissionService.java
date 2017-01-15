package no.difi.oxalis.api.outbound;

import brave.Span;
import eu.peppol.lang.OxalisTransmissionException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author erlend
 */
public interface TransmissionService {

    TransmissionResponse send(InputStream inputStream) throws IOException, OxalisTransmissionException;

    default TransmissionResponse send(InputStream inputStream, Span root) throws IOException, OxalisTransmissionException {
        return send(inputStream);
    }

}
