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
import network.oxalis.vefa.peppol.common.model.Header;

import java.io.InputStream;
import java.io.Serializable;

/**
 * @author erlend
 * @since 4.0.0
 */
class DefaultTransmissionMessage implements TransmissionMessage, Serializable {

    private static final long serialVersionUID = -2292244133544793106L;

    private final Tag tag;

    private final Header header;

    private final InputStream payload;

    public DefaultTransmissionMessage(Header header, InputStream payload, Tag tag) {
        this.tag = tag;
        this.header = header;
        this.payload = payload;
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
}
