package no.difi.oxalis.commons.inbound;

import no.difi.oxalis.api.inbound.InboundVerifier;
import no.difi.vefa.peppol.common.model.Header;

public class DefaultInboundVerifier implements InboundVerifier {

    @Override
    public void verify(Header header) {
        // No action.
    }
}
