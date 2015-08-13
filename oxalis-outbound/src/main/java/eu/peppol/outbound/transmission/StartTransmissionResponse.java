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
public class StartTransmissionResponse extends AbstractTransmissionResponse implements TransmissionResponse {

    public StartTransmissionResponse(TransmissionId transmissionId, PeppolStandardBusinessHeader sbdh, URL url, BusDoxProtocol busDoxProtocol) {
        super(transmissionId, sbdh, url, busDoxProtocol, new CommonName("")); // TODO find out how to get CN from Metro / JAX-WS
    }

}
