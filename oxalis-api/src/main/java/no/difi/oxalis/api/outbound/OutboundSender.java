package no.difi.oxalis.api.outbound;

import brave.Span;
import eu.peppol.lang.OxalisTransmissionException;

import java.io.InputStream;

public interface OutboundSender {

    // TODO
    Object send(InputStream inputStream) throws OxalisTransmissionException;

    default Object send(InputStream inputStream, Span span) throws OxalisTransmissionException {
        return send(inputStream);
    }
}
