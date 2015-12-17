/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

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
