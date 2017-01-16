package no.difi.oxalis.api.lookup;

import brave.Span;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;

/**
 * Defines a standardized lookup service for use in Oxalis.
 *
 * @author erlend
 * @since 4.0.0
 */
public interface LookupService {

    /**
     * Performs lookup using metadata from content to be sent.
     *
     * @param header Metadata from content.
     * @return Endpoint information to be used when transmitting content.
     * @throws OxalisTransmissionException Thrown if no endpoint metadata were detected using metadata.
     */
    Endpoint lookup(Header header) throws OxalisTransmissionException;

    /**
     * Performs lookup using metadata from content to be sent.
     *
     * @param header Metadata from content.
     * @param root   Current trace.
     * @return Endpoint information to be used when transmitting content.
     * @throws OxalisTransmissionException Thrown if no endpoint metadata were detected using metadata.
     */
    default Endpoint lookup(Header header, Span root) throws OxalisTransmissionException {
        return lookup(header);
    }
}
