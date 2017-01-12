package eu.peppol.outbound.api;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import eu.peppol.smp.SmpLookupManager;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;

import java.io.InputStream;

public interface TransmissionRequest {

    @Deprecated
    PeppolStandardBusinessHeader getPeppolStandardBusinessHeader();

    Header getHeader();

    InputStream getPayload();

    @Deprecated
    SmpLookupManager.PeppolEndpointData getEndpointAddress();

    // Endpoint getEndpoint();

    boolean isTraceEnabled();

    MessageId getMessageId();
}
