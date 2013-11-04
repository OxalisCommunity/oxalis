package eu.peppol.outbound.transmission;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.smp.SmpLookupManager;

import java.io.InputStream;

/**
 * @author steinar
 *         Date: 04.11.13
 *         Time: 10:02
 */
public class TransmissionRequest {


    private final PeppolStandardBusinessHeader peppolStandardBusinessHeader;

    public TransmissionRequest(TransmissionRequestBuilder transmissionRequestBuilder) {
        peppolStandardBusinessHeader = transmissionRequestBuilder.getPeppolStandardBusinessHeader();
    }
}
