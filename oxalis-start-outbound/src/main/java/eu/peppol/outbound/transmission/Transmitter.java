package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.start.identifier.AccessPointIdentifier;
import eu.peppol.start.identifier.PeppolMessageHeader;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;

/**
 * Executes transmission requests by sending the payload to the requested destination.
 *
 * <ol>
 *     <li>Logs the fact that we are about to send</li>
 *     <li>Logs the outcome of the transmission</li>
 * </ol>
 *
 * @author steinar
 *         Date: 04.11.13
 *         Time: 17:26
 */
public class Transmitter {

    private final MessageSenderFactory messageSenderFactory;
    private final RawStatisticsRepository rawStatisticsRepository;

    @Inject
    public Transmitter(MessageSenderFactory messageSenderFactory, RawStatisticsRepository rawStatisticsRepository) {
        this.messageSenderFactory = messageSenderFactory;
        this.rawStatisticsRepository = rawStatisticsRepository;
    }


    public void transmit(TransmissionRequest transmissionRequest) {

        // TODO: add some logging to the database here
        MessageSender messageSender = messageSenderFactory.createMessageSender(transmissionRequest.getEndpointAddress().getBusDoxProtocol());

        messageSender.send(transmissionRequest);

        // TODO: log some extra stuff here for completed transmission


    }

    // TODO: implement this
    void persistStatistics(PeppolMessageHeader messageHeader) {

        RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder()
                .accessPointIdentifier(new AccessPointIdentifier("dumbo"))   // Identifier predefined in Oxalis global config file
                .outbound()
                .documentType(messageHeader.getDocumentTypeIdentifier())
                .sender(messageHeader.getSenderId())
                .receiver(messageHeader.getRecipientId())
                .profile(messageHeader.getPeppolProcessTypeId())
                .channel(messageHeader.getChannelId())
                .build();
        rawStatisticsRepository.persist(rawStatistics);
    }

}
