package eu.peppol.outbound.transmission;

import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.security.CommonName;

import java.net.URL;

/**
 * @author steinar
 * @author thore
 */
public class As2TransmissionResponse extends AbstractTransmissionResponse implements TransmissionResponse {

    public As2TransmissionResponse(TransmissionId transmissionId, PeppolStandardBusinessHeader peppolStandardBusinessHeader, URL url, BusDoxProtocol busDoxProtocol, CommonName commonName) {
        super(transmissionId, peppolStandardBusinessHeader, url, busDoxProtocol, commonName);
    }

}
