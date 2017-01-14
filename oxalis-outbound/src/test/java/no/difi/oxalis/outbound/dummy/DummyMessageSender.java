package no.difi.oxalis.outbound.dummy;

import com.google.common.io.ByteStreams;
import eu.peppol.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;

import java.io.IOException;

public class DummyMessageSender implements MessageSender {

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        try {
            ByteStreams.exhaust(transmissionRequest.getPayload());

            return new DummyTransmissionResponse(transmissionRequest);
        } catch (IOException e) {
            throw new OxalisTransmissionException(e.getMessage(), e);
        }
    }
}
