package eu.peppol.identifier;

import java.net.URI;
import java.security.cert.X509Certificate;

/**
 * Representation of receiving endpoint.
 */
public interface Endpoint {

    /**
     * Transport profile to use when communicating with this endpoint.
     *
     * @return Transport profile to use.
     */
    String getTransportProfile();

    /**
     * Address of receiving endpoint.
     *
     * @return URI representing the address to use.
     */
    URI getAddress();

    /**
     * Certificate of receiving endpoint.
     *
     * @return Certificate to use.
     */
    X509Certificate getCertificate();
}
