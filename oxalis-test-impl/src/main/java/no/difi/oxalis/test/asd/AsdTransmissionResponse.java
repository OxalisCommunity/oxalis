package no.difi.oxalis.test.asd;

import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.common.model.*;

import java.util.Date;
import java.util.List;

/**
 * @author erlend
 */
public class AsdTransmissionResponse implements TransmissionResponse {

    private Endpoint endpoint;

    private Header header;

    private TransmissionIdentifier transmissionIdentifier;

    public AsdTransmissionResponse(TransmissionRequest transmissionRequest, TransmissionIdentifier transmissionIdentifier) {
        this.endpoint = transmissionRequest.getEndpoint();
        this.header = transmissionRequest.getHeader();
        this.transmissionIdentifier = transmissionIdentifier;
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public TransmissionIdentifier getTransmissionIdentifier() {
        return transmissionIdentifier;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public Digest getDigest() {
        return null;
    }

    @Override
    public TransportProtocol getTransportProtocol() {
        return AsdConstants.TRANSPORT_PROTOCOL;
    }

    @Override
    public List<Receipt> getReceipts() {
        return null;
    }

    @Override
    public Receipt primaryReceipt() {
        return null;
    }
}
