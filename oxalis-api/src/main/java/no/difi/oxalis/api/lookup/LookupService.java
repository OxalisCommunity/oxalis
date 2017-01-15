package no.difi.oxalis.api.lookup;

import brave.Span;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;

public interface LookupService {

    Endpoint lookup(Header header) throws OxalisTransmissionException;

    default Endpoint lookup(Header header, Span span) throws OxalisTransmissionException {
        return lookup(header);
    }
}
