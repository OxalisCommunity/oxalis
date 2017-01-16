package no.difi.oxalis.api.outbound;

import brave.Span;
import eu.peppol.lang.OxalisTransmissionException;

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
 */
public interface TransmissionService {

    /**
     * Sends content found in the InputStream.
     *
     * @param inputStream InputStream containing content to be sent.
     * @return Transmission response containing information from the performed transmission.
     * @throws IOException                 Thrown on any IO exception.
     * @throws OxalisTransmissionException Thrown if there were any problems making Oxalis unable to send the content.
     */
    TransmissionResponse send(InputStream inputStream) throws IOException, OxalisTransmissionException;

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
            throws IOException, OxalisTransmissionException {
        return send(inputStream);
    }

}
