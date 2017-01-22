package no.difi.oxalis.inbound.verifier;

import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.inbound.InboundVerifier;
import no.difi.vefa.peppol.common.model.Header;

/**
 * Default implementation allowing all incoming transmissions.
 *
 * @author erlend
 * @since 4.0.0
 */
public class DefaultVerifier implements InboundVerifier {

    @Override
    public void verify(MessageId messageId, Header header) {
        // No action.
    }
}
