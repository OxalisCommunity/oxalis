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

package no.difi.oxalis.api.outbound;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import eu.peppol.security.CommonName;
import no.difi.vefa.peppol.common.model.*;

import java.net.URI;
import java.util.List;

/**
 * @author steinar
 * @author thore
 * @author erlend
 * @since 4.0.0
 */
public interface TransmissionResponse {

    /**
     * Transmission id assigned during transmission
     *
     * @return
     */
    MessageId getMessageId();

    Endpoint getEndpoint();

    Header getHeader();

    Digest getDigest();

    Receipt primaryReceipt();

    List<Receipt> getReceipts();


    /**
     * Get the effective SBDH used during transmission
     *
     * @return
     */
    @Deprecated
    default PeppolStandardBusinessHeader getStandardBusinessHeader() {
        return new PeppolStandardBusinessHeader(getHeader());
    }

    /**
     * The destination URL for the transmission
     *
     * @return
     */
    @Deprecated
    default URI getURL() {
        return getEndpoint().getAddress();
    }

    /**
     * The protocol used for the transmission
     *
     * @return
     */
    @Deprecated
    default TransportProfile getProtocol() {
        return getEndpoint().getTransportProfile();
    }

    /**
     * Provides access to the native transmission evidence like for instance the MDN for AS2
     *
     * @return
     */
    @Deprecated
    default byte[] getNativeEvidenceBytes() {
        return primaryReceipt().getValue();
    }
}
