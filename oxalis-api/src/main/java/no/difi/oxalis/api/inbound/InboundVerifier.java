package no.difi.oxalis.api.inbound;

import no.difi.vefa.peppol.common.model.Header;

/**
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface InboundVerifier {

    void verify(Header header);

}
