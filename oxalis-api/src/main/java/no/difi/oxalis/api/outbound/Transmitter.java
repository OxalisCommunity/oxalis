/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

import brave.Span;
import eu.peppol.lang.OxalisTransmissionException;

/**
 * Interface defining contract of Transmitter. A transmitter instance is multi-protocol, and transmits content of
 * transmission request based on requested transport profile.
 *
 * @author steinar
 *         Date: 18.11.2016
 *         Time: 16.21
 * @author erlend
 */
public interface Transmitter {

    /**
     * Transmit content of transmission request. (No tracing.)
     *
     * @param transmissionRequest Content to be transmitted.
     * @return Result of transmission.
     * @throws OxalisTransmissionException Thrown when transmission fails.
     */
    TransmissionResponse transmit(TransmissionRequest transmissionRequest) throws OxalisTransmissionException;

    /**
     * Transmit content of transmission request. (With tracing.)
     *
     * @param transmissionRequest Content to be transmitted.
     * @param root                Current trace.
     * @return Result of transmission.
     * @throws OxalisTransmissionException Thrown when transmission fails.
     */
    default TransmissionResponse transmit(TransmissionRequest transmissionRequest, Span root)
            throws OxalisTransmissionException {
        return transmit(transmissionRequest);
    }
}
