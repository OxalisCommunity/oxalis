package no.difi.oxalis.outbound.dummy;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import eu.peppol.security.CommonName;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.Receipt;
import no.difi.vefa.peppol.common.model.TransportProfile;

import java.net.URI;
import java.util.List;

public class DummyTransmissionResponse implements TransmissionResponse {

    private TransmissionRequest transmissionRequest;

    public DummyTransmissionResponse(TransmissionRequest transmissionRequest) {
        this.transmissionRequest = transmissionRequest;
    }

    @Override
    public MessageId getMessageId() {
        return transmissionRequest.getMessageId();
    }

    @Override
    public PeppolStandardBusinessHeader getStandardBusinessHeader() {
        return transmissionRequest.getPeppolStandardBusinessHeader();
    }

    @Override
    public Header getHeader() {
        return transmissionRequest.getHeader();
    }

    @Override
    public URI getURL() {
        return transmissionRequest.getEndpoint().getAddress();
    }

    @Override
    public TransportProfile getProtocol() {
        return transmissionRequest.getEndpoint().getTransportProfile();
    }

    @Override
    public CommonName getCommonName() {
        return null;
    }

    @Override
    public byte[] getRemEvidenceBytes() {
        return new byte[0];
    }

    @Override
    public byte[] getNativeEvidenceBytes() {
        return new byte[0];
    }

    @Override
    public List<Receipt> getReceipts() {
        return null;
    }
}
