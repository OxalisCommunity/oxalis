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

package network.oxalis.api.outbound;

import io.opentracing.Span;
import network.oxalis.api.lang.OxalisTransmissionException;

/**
 * Interface defining contract of Transmitter. A transmitter instance is multi-protocol, and transmits content of
 * transmission request based on requested transport profile.
 *
 * @author steinar
 *         Date: 18.11.2016
 *         Time: 16.21
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface Transmitter {

    /**
     * Transmit content of transmission request. (No tracing.)
     *
     * @param transmissionMessage Content to be transmitted.
     * @return Result of transmission.
     * @throws OxalisTransmissionException Thrown when transmission fails.
     */
    TransmissionResponse transmit(TransmissionMessage transmissionMessage) throws OxalisTransmissionException;

    /**
     * Transmit content of transmission request. (With tracing.)
     *
     * @param transmissionMessage Content to be transmitted.
     * @param root                Current trace.
     * @return Result of transmission.
     * @throws OxalisTransmissionException Thrown when transmission fails.
     */
    default TransmissionResponse transmit(TransmissionMessage transmissionMessage, Span root)
            throws OxalisTransmissionException {
        return transmit(transmissionMessage);
    }
}
