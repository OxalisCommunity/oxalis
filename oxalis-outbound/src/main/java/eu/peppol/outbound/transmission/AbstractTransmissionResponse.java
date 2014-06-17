package eu.peppol.outbound.transmission;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.TransmissionId;

/**
 * @author steinar
 *         Date: 08.11.13
 *         Time: 09:54
 */
public class AbstractTransmissionResponse {

    TransmissionId transmissionId;
    private final PeppolStandardBusinessHeader sbdh;

    public AbstractTransmissionResponse(TransmissionId transmissionId, PeppolStandardBusinessHeader sbdh) {
        this.transmissionId = transmissionId;
        this.sbdh = sbdh;
    }

    public TransmissionId getTransmissionId() {
        return transmissionId;
    }

    public PeppolStandardBusinessHeader getStandardBusinessHeader() {
        return sbdh;
    }

}
