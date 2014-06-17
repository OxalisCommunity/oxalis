package eu.peppol.outbound.transmission;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.TransmissionId;

/**
 * @author steinar
 *         Date: 08.11.13
 *         Time: 10:52
 */
public class As2TransmissionResponse extends AbstractTransmissionResponse implements TransmissionResponse {

    public As2TransmissionResponse(TransmissionId transmissionId, PeppolStandardBusinessHeader peppolStandardBusinessHeader) {
        super(transmissionId, peppolStandardBusinessHeader);
    }

}
