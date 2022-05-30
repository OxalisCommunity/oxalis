package network.oxalis.ext.testbed.v1;

import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.tag.Tag;
import network.oxalis.commons.security.CertificateUtils;
import network.oxalis.ext.testbed.v1.jaxb.DestinationType;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.common.model.TransportProfile;

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
