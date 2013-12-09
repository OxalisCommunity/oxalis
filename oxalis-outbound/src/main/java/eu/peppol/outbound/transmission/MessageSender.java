package eu.peppol.outbound.transmission;

/**
 * @author steinar
 *         Date: 31.10.13
 *         Time: 14:25
 */
public interface MessageSender {

    TransmissionResponse send(TransmissionRequest transmissionRequest);

}
