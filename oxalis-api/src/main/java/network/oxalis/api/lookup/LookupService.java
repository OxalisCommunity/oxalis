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

package network.oxalis.api.lookup;

import io.opentracing.Span;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.Header;

/**
 * Defines a standardized lookup service for use in Oxalis.
 *
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface LookupService {

    /**
     * Performs lookup using metadata from content to be sent.
     *
     * @param header Metadata from content.
     * @return Endpoint information to be used when transmitting content.
     * @throws OxalisTransmissionException Thrown if no endpoint metadata were detected using metadata.
     */
    Endpoint lookup(Header header) throws OxalisTransmissionException;

    /**
     * Performs lookup using metadata from content to be sent.
     *
     * @param header Metadata from content.
     * @param root   Current trace.
     * @return Endpoint information to be used when transmitting content.
     * @throws OxalisTransmissionException Thrown if no endpoint metadata were detected using metadata.
     */
    default Endpoint lookup(Header header, Span root) throws OxalisTransmissionException {
        return lookup(header);
    }
}
