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

package eu.peppol.outbound.api;

import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.MessageId;
import eu.peppol.security.CommonName;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.Receipt;

import java.net.URL;
import java.util.List;

/**
 * @author steinar
 * @author thore
 */
public interface TransmissionResponse {

    /**
     * Transmission id assigned during transmission
     */
    MessageId getMessageId();

    /**
     * Get the effective SBDH used during transmission
     */
    @Deprecated
    PeppolStandardBusinessHeader getStandardBusinessHeader();

    Header getHeader();

    /**
     * The destination URL for the transmission
     */
    URL getURL();

    // Endpoint getEndpoint();

    /**
     * The protocol used for the transmission
     */
    BusDoxProtocol getProtocol();

    /**
     * The common name of the receiver certificate
     */
    CommonName getCommonName();

    /**
     * The REM evidence produced.
     */
    @Deprecated
    byte[] getRemEvidenceBytes();

    /** Provides access to the native transmission evidence like for instance the MDN for AS2 */
    @Deprecated
    byte[] getNativeEvidenceBytes();

    List<Receipt> getReceipts();
}
