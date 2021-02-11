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
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.tag.Tag;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a standardized transmission service interface accepting the InputStream of the content to be sent.
 * <p>
 * Typical implementation:
 * <pre>
 * {@code
 * public TransmissionResponse send(InputStream inputStream) throws IOException, OxalisTransmissionException {
 *      TransmissionRequestFactory transmissionRequestFactory = // Fetch or find locally.
 *      TransmissionRequest transmissionRequest = transmissionRequestFactory.newInstance(inputStream);
 *
 *      Transmitter transmitter = // Fetch or find locally.
 *      TransmissionResponse transmissionResponse = transmitter.transmit(transmissionRequest)
 *
 *      return transmissionResponse;
 * }
 * }
 * </pre>
 *
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface TransmissionService {

    /**
     * Sends content found in the InputStream.
     *
     * @param inputStream InputStream containing content to be sent.
     * @return Transmission response containing information from the performed transmission.
     * @throws IOException                 Thrown on any IO exception.
     * @throws OxalisTransmissionException Thrown if there were any problems making Oxalis unable to send the content.
     */
    default TransmissionResponse send(InputStream inputStream)
            throws IOException, OxalisTransmissionException, OxalisContentException {
        return send(inputStream, Tag.NONE);
    }

    /**
     * Sends content found in the InputStream.
     *
     * @param inputStream InputStream containing content to be sent.
     * @param tag         Tag defined by client.
     * @return Transmission response containing information from the performed transmission.
     * @throws IOException                 Thrown on any IO exception.
     * @throws OxalisTransmissionException Thrown if there were any problems making Oxalis unable to send the content.
     */
    TransmissionResponse send(InputStream inputStream, Tag tag)
            throws IOException, OxalisTransmissionException, OxalisContentException;

    /**
     * Sends content found in the InputStream.
     *
     * @param inputStream InputStream containing content to be sent.
     * @param root        Current trace.
     * @return Transmission response containing information from the performed transmission.
     * @throws IOException                 Thrown on any IO exception.
     * @throws OxalisTransmissionException Thrown if there were any problems making Oxalis unable to send the content.
     */
    default TransmissionResponse send(InputStream inputStream, Span root)
            throws IOException, OxalisTransmissionException, OxalisContentException {
        return send(inputStream, Tag.NONE);
    }

    /**
     * Sends content found in the InputStream.
     *
     * @param inputStream InputStream containing content to be sent.
     * @param tag         Tag defined by client.
     * @param root        Current trace.
     * @return Transmission response containing information from the performed transmission.
     * @throws IOException                 Thrown on any IO exception.
     * @throws OxalisTransmissionException Thrown if there were any problems making Oxalis unable to send the content.
     */
    default TransmissionResponse send(InputStream inputStream, Tag tag, Span root)
            throws IOException, OxalisTransmissionException, OxalisContentException {
        return send(inputStream, tag);
    }

}
