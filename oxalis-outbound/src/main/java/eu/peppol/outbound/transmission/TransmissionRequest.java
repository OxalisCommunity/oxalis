package eu.peppol.outbound.transmission;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.smp.SmpLookupManager;

/**
 * Describes a request to transmit a payload (PEPPOL Document) to a designated end-point.
 * Instances of this class are to be deemed as value objects, as they are immutable.
 *
 * @author steinar
 * @author thore
 */
public class TransmissionRequest {

    private final PeppolStandardBusinessHeader peppolStandardBusinessHeader;
    private final byte[] payload;
    private final SmpLookupManager.PeppolEndpointData endpointAddress;
    private boolean traceEnabled;

    /**
     * Module private constructor grabbing the constructor data from the supplied builder.
     *
     * @param transmissionRequestBuilder
     */
    TransmissionRequest(TransmissionRequestBuilder transmissionRequestBuilder) {
        this.peppolStandardBusinessHeader = transmissionRequestBuilder.getEffectiveStandardBusinessHeader();
        this.payload = transmissionRequestBuilder.getPayload();
        this.endpointAddress = transmissionRequestBuilder.getEndpointAddress();
        this.traceEnabled = transmissionRequestBuilder.isTraceEnabled();
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

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

}
