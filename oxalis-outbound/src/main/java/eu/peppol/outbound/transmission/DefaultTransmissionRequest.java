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
import eu.peppol.identifier.MessageId;
import eu.peppol.smp.PeppolEndpointData;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;

import java.io.InputStream;

/**
 * Describes a request to transmit a payload (PEPPOL Document) to a designated end-point.
 * Instances of this class are to be deemed as value objects, as they are immutable.
 *
 * @author steinar
 * @author thore
 */
class DefaultTransmissionRequest implements TransmissionRequest {

    private final PeppolStandardBusinessHeader peppolStandardBusinessHeader;

    private final Header header;

    private final InputStream payload;

    private final PeppolEndpointData endpointAddress;

    private final Endpoint endpoint;

    private boolean traceEnabled;

    private final MessageId messageId;

    /**
     * Module private constructor grabbing the constructor data from the supplied builder.
     */
    DefaultTransmissionRequest(TransmissionRequestBuilder transmissionRequestBuilder) {
        this.peppolStandardBusinessHeader = transmissionRequestBuilder.getEffectiveStandardBusinessHeader();
        this.header = peppolStandardBusinessHeader.toVefa();
        this.payload = transmissionRequestBuilder.getPayload();
        this.endpointAddress = transmissionRequestBuilder.getEndpointAddress();
        this.endpoint = null;
        this.traceEnabled = transmissionRequestBuilder.isTraceEnabled();
        this.messageId = transmissionRequestBuilder.getMessageId();
    }

    DefaultTransmissionRequest(Header header, InputStream inputStream, Endpoint endpoint) {
        this.peppolStandardBusinessHeader = new PeppolStandardBusinessHeader(header);
        this.header = header;
        this.payload = inputStream;
        this.endpointAddress = new PeppolEndpointData(endpoint);
        this.endpoint = endpoint;
        this.traceEnabled = false;
        this.messageId = new MessageId();
    }

    @Override
    public PeppolStandardBusinessHeader getPeppolStandardBusinessHeader() {
        return peppolStandardBusinessHeader;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public InputStream getPayload() {
        return payload;
    }

    @Override
    public PeppolEndpointData getEndpointAddress() {
        return endpointAddress;
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    @Override
    public MessageId getMessageId() {
        return messageId;
    }
}
