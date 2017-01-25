/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package no.difi.oxalis.api.outbound;

import eu.peppol.PeppolStandardBusinessHeader;
import no.difi.oxalis.api.transmission.TransmissionResult;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.Receipt;
import no.difi.vefa.peppol.common.model.TransportProfile;

import java.util.List;

/**
 * @author steinar
 * @author thore
 * @author erlend
 * @since 4.0.0
 */
public interface TransmissionResponse extends TransmissionResult {

    Endpoint getEndpoint();

    /**
     * {@inheritDoc}
     */
    @Override
    default TransportProfile getProtocol() {
        return getEndpoint().getTransportProfile();
    }


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
     * Provides access to the native transmission evidence like for instance the MDN for AS2
     *
     * @return
     */
    @Deprecated
    default byte[] getNativeEvidenceBytes() {
        return primaryReceipt().getValue();
    }
}
