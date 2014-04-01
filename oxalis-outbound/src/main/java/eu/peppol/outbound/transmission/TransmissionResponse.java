package eu.peppol.outbound.transmission;

import eu.peppol.identifier.TransmissionId;

/**
 * @author steinar
 */
public interface TransmissionResponse {

    /**
     * Transmission id assigned during transmission
     */
    TransmissionId getTransmissionId();

}
