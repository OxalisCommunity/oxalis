package no.difi.oxalis.api.outbound;

import eu.peppol.identifier.MessageId;
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

}
