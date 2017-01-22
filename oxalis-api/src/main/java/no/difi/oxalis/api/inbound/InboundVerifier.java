package no.difi.oxalis.api.inbound;

import eu.peppol.identifier.MessageId;
import no.difi.vefa.peppol.common.model.Header;

/**
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface InboundVerifier {

    void verify(MessageId messageId, Header header);

}
