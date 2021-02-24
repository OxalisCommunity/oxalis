/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.outbound.transmission;

import network.oxalis.api.tag.Tag;
import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.Header;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * Describes a request to transmit a payload (PEPPOL Document) to a designated end-point.
 * Instances of this class are to be deemed as value objects, as they are immutable.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
class DefaultTransmissionRequest implements TransmissionRequest, Serializable {

    private static final long serialVersionUID = -4542158917465140099L;

    private final Tag tag;

    private final Endpoint endpoint;

    private final Header header;

    private final InputStream payload;

    /**
     * Module private constructor grabbing the constructor data from the supplied builder.
     */
    public DefaultTransmissionRequest(Header header, InputStream inputStream, Endpoint endpoint, Tag tag) {
        this.tag = tag;
        this.endpoint = endpoint;
        this.header = header;
        this.payload = inputStream;
    }

    public DefaultTransmissionRequest(TransmissionMessage transmissionMessage, Endpoint endpoint) {
        this.endpoint = endpoint;
        this.tag = transmissionMessage.getTag();
        this.header = transmissionMessage.getHeader();
        this.payload = transmissionMessage.getPayload();
    }

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public Tag getTag() {
        return tag;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultTransmissionRequest that = (DefaultTransmissionRequest) o;
        return Objects.equals(tag, that.tag) &&
                Objects.equals(endpoint, that.endpoint) &&
                Objects.equals(header, that.header) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, endpoint, header, payload);
    }
}
