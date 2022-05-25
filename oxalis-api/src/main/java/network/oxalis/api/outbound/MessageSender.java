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
 * MessageSender is implemented by a specific protocol implementation.
 *
 * @author steinar
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface MessageSender {

    /**
     * Protocol specific transmission of transmission requested. (Without tracing.)
     *
     * @param transmissionRequest Requested transmission to take place.
     * @return Response content of a successful transmission.
     * @throws OxalisTransmissionException Thrown when transmission was not sent according to protocol specific rules or
     *                                     because something went wrong during transmission.
     */
    TransmissionResponse send(TransmissionRequest transmissionRequest) throws OxalisTransmissionException;

    /**
     * Protocol specific transmission of transmission requested. (With tracing.)
     *
     * @param transmissionRequest Requested transmission to take place.
     * @param root                Current trace.
     * @return Response content of a successful transmission.
     * @throws OxalisTransmissionException Thrown when transmission was not sent according to protocol specific rules or
     *                                     because something went wrong during transmission.
     */
    default TransmissionResponse send(TransmissionRequest transmissionRequest, Span root)
            throws OxalisTransmissionException {
        return send(transmissionRequest);
    }

}
