package eu.peppol.service;

import eu.peppol.identifier.Endpoint;
import eu.peppol.identifier.MessageHeader;
import eu.peppol.lang.OxalisLookupException;
import eu.peppol.lang.OxalisSecurityException;

public interface LookupService {

    /**
     * Fetch endpoint information for a receiving endpoint.
     *
     * @param header Header of message to send.
     * @param transportProfiles Supported transport profiles by current Oxalis instance.
     * @return Endpoint information for use when sending.
     * @throws OxalisLookupException Thrown in case of unable to lookup, lack of supporting transport profiles and more.
     * @throws OxalisSecurityException Thrown in case of security reasons like invalid certificate and signature errors.
     */
    Endpoint getEndpoint(MessageHeader header, /* TransportProfile */ String... transportProfiles) throws OxalisLookupException, OxalisSecurityException;

}
