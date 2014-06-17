package eu.peppol.outbound.transmission;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.TransmissionId;

/**
 * @author steinar
 *         Date: 08.11.13
 *         Time: 09:48
 */
public class StartTransmissionResponse  extends AbstractTransmissionResponse implements TransmissionResponse {

    public StartTransmissionResponse(TransmissionId transmissionId, PeppolStandardBusinessHeader sbdh) {
        super(transmissionId, sbdh);
    }

}
