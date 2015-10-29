package no.difi.oxalis.smp.identifier;

import no.difi.vefa.peppol.common.model.Endpoint;

import java.net.URI;
import java.security.cert.X509Certificate;

/**
 * Wrapper making Endpoint representation in VEFA library available to Oxalis.
 */
public class EndpointWrapper implements eu.peppol.identifier.Endpoint {

    /**
     * Endpoint information fetched from SMP.
     */
    private Endpoint endpoint;

    public EndpointWrapper(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTransportProfile() {
        return endpoint.getTransportProfile().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getAddress() {
        return URI.create(endpoint.getAddress());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public X509Certificate getCertificate() {
        return endpoint.getCertificate();
    }
}
