package eu.peppol.outbound.transmission;

/**
 * @author steinar
 *         Date: 31.10.13
 *         Time: 14:25
 */
public interface MessageSender {

    MessageResponse send(TransmissionRequest transmissionRequest);

}
