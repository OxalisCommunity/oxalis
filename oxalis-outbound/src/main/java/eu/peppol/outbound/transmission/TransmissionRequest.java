package eu.peppol.outbound.transmission;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.smp.SmpLookupManager;

/**
 * Describes a request to transmit a payload (PEPPOL Document) to a designated end-point.
 *
 * Instances of this class are to be deemed as value objects, as they are immutable.
 *
 * @author steinar
 *         Date: 04.11.13
 *         Time: 10:02
 */
public class TransmissionRequest {


    private final PeppolStandardBusinessHeader peppolStandardBusinessHeader;
    private final byte[] payload;
    private final SmpLookupManager.PeppolEndpointData endpointAddress;

    /**
     * Module private constructor grabbing the constructor data from the supplied builder.
     *
     * @param transmissionRequestBuilder
     */
    TransmissionRequest(TransmissionRequestBuilder transmissionRequestBuilder) {
        peppolStandardBusinessHeader = transmissionRequestBuilder.getEffectiveStandardBusinessHeader();
        payload = transmissionRequestBuilder.getPayload();
        endpointAddress = transmissionRequestBuilder.getEndpointAddress();
    }

    public PeppolStandardBusinessHeader getPeppolStandardBusinessHeader() {
        return peppolStandardBusinessHeader;
    }

    public byte[] getPayload() {
        return payload;
    }

    public SmpLookupManager.PeppolEndpointData getEndpointAddress() {
        return endpointAddress;
    }
}
