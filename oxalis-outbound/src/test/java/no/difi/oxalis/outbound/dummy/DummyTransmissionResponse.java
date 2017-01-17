package no.difi.oxalis.outbound.dummy;

import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.common.model.Digest;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.Receipt;

import java.util.Collections;
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
    public Header getHeader() {
        return transmissionRequest.getHeader();
    }

    @Override
    public List<Receipt> getReceipts() {
        return Collections.emptyList();
    }

    @Override
    public Endpoint getEndpoint() {
        return transmissionRequest.getEndpoint();
    }

    @Override
    public Receipt primaryReceipt() {
        return Receipt.of(new byte[0]);
    }

    @Override
    public Digest getDigest() {
        return null;
    }
}
