package no.difi.oxalis.ext.testbed.v1;

import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.tag.Tag;
import no.difi.oxalis.commons.security.CertificateUtils;
import no.difi.oxalis.ext.testbed.v1.jaxb.DestinationType;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.TransportProfile;

import java.io.InputStream;
import java.net.URI;
import java.security.cert.CertificateException;

/**
 * @author erlend
 */
public class TestbedTransmissionRequest implements TransmissionRequest {

    private TransmissionMessage transmissionMessage;

    private Endpoint endpoint;

    public TestbedTransmissionRequest(TransmissionMessage transmissionMessage, DestinationType destination)
            throws CertificateException {
        this(transmissionMessage, Endpoint.of(
                TransportProfile.of(destination.getTransportProfile()),
                URI.create(destination.getURI()),
                CertificateUtils.parseCertificate(destination.getCertificate())
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
