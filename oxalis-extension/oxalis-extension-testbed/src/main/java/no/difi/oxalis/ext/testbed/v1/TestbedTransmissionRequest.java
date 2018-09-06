package no.difi.oxalis.ext.testbed.v1;

import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.tag.Tag;
import no.difi.oxalis.ext.testbed.v1.jaxb.DestinationType;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @author erlend
 */
public class TestbedTransmissionRequest implements TransmissionRequest {

    private static final CertificateFactory CERTIFICATE_FACTORY;

    private TransmissionMessage transmissionMessage;

    private Endpoint endpoint;

    static {
        try {
            CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public TestbedTransmissionRequest(TransmissionMessage transmissionMessage, DestinationType destination)
            throws CertificateException {
        this(transmissionMessage, Endpoint.of(
                TransportProfile.of(destination.getTransportProfile()),
                URI.create(destination.getURI()),
                (X509Certificate) CERTIFICATE_FACTORY.generateCertificate(new ByteArrayInputStream(destination.getCertificate()))
        ));
    }

    public TestbedTransmissionRequest(TransmissionMessage transmissionMessage, Endpoint endpoint) {
        this.transmissionMessage = transmissionMessage;
        this.endpoint = endpoint;
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public Header getHeader() {
        return transmissionMessage.getHeader();
    }

    @Override
    public InputStream getPayload() {
        return transmissionMessage.getPayload();
    }

    @Override
    public Tag getTag() {
        return transmissionMessage.getTag();
    }
}
