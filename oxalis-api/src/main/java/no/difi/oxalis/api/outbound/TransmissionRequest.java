package no.difi.oxalis.api.outbound;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import eu.peppol.smp.PeppolEndpointData;
import no.difi.vefa.peppol.common.model.Header;

import java.io.InputStream;

public interface TransmissionRequest {

    @Deprecated
    PeppolStandardBusinessHeader getPeppolStandardBusinessHeader();

    Header getHeader();

    InputStream getPayload();

    @Deprecated
    PeppolEndpointData getEndpointAddress();

    // Endpoint getEndpoint();

    boolean isTraceEnabled();

    MessageId getMessageId();
}
