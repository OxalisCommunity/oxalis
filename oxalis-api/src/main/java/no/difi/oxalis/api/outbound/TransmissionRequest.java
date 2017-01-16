package no.difi.oxalis.api.outbound;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import eu.peppol.smp.PeppolEndpointData;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;

import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.0
 */
public interface TransmissionRequest {

    MessageId getMessageId();

    Endpoint getEndpoint();

    Header getHeader();

    InputStream getPayload();


    @Deprecated
    default PeppolStandardBusinessHeader getPeppolStandardBusinessHeader() {
        return new PeppolStandardBusinessHeader(getHeader());
    }

    @Deprecated
    default PeppolEndpointData getEndpointAddress() {
        return new PeppolEndpointData(getEndpoint());
    }
}
