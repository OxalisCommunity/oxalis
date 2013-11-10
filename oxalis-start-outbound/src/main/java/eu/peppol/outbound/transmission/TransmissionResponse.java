package eu.peppol.outbound.transmission;

import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.TransmissionId;

/**
 * @author steinar
 *         Date: 31.10.13
 *         Time: 14:44
 */
public interface TransmissionResponse {

    /** Transmission id assigned during transmission */
    TransmissionId getTransmissionId();
}
