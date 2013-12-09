package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.security.CommonName;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;

import java.util.Date;

/**
 * Executes transmission requests by sending the payload to the requested destination.
 * <p/>
 * <ol>
 * <li>Logs the fact that we are about to send</li>
 * <li>Logs the outcome of the transmission</li>
 * </ol>
 *
 * @author steinar
 *         Date: 04.11.13
 *         Time: 17:26
 */
public class Transmitter {

    private final MessageSenderFactory messageSenderFactory;
    private final RawStatisticsRepository rawStatisticsRepository;
    private final CommonName ourCommonName;
    private AccessPointIdentifier ourAccessPointIdentifier;


    @Inject
    public Transmitter(MessageSenderFactory messageSenderFactory, RawStatisticsRepository rawStatisticsRepository, @Named("OurCommonName")CommonName ourCommonName) {
        this.messageSenderFactory = messageSenderFactory;
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.ourCommonName = ourCommonName;
        if (ourCommonName == null) {
            throw new IllegalArgumentException("Must supply the Common Name (CN) for our access point");
        }
        ourAccessPointIdentifier = new AccessPointIdentifier(ourCommonName.toString());
    }


    public TransmissionResponse transmit(TransmissionRequest transmissionRequest) {

        BusDoxProtocol busDoxProtocol = transmissionRequest.getEndpointAddress().getBusDoxProtocol();
        MessageSender messageSender = messageSenderFactory.createMessageSender(busDoxProtocol);

        TransmissionResponse transmissionResponse = messageSender.send(transmissionRequest);

        persistStatistics(transmissionRequest, transmissionResponse);

        return transmissionResponse;
    }

    void persistStatistics(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse) {


        RawStatistics.RawStatisticsBuilder builder = new RawStatistics.RawStatisticsBuilder()
                // TODO: replace AP identifier with Common Name (CN) of the culprit
                .accessPointIdentifier(ourAccessPointIdentifier)   // Identifier predefined in Oxalis global config file
                .outbound()
                .documentType(transmissionRequest.getPeppolStandardBusinessHeader().getDocumentTypeIdentifier())
                .sender(transmissionRequest.getPeppolStandardBusinessHeader().getSenderId())
                .receiver(transmissionRequest.getPeppolStandardBusinessHeader().getRecipientId())
                .profile(transmissionRequest.getPeppolStandardBusinessHeader().getProfileTypeIdentifier())
                .date(new Date());  // Time stamp of reception

        // If we know the CN name of the destination AP, supply that as the channel id
        if (transmissionRequest.getEndpointAddress().getCommonName() != null) {
            String accessPointIdentifierValue = transmissionRequest.getEndpointAddress().getCommonName().toString();
            builder.channel(new ChannelId(accessPointIdentifierValue));
        }

        RawStatistics rawStatistics = builder.build();

        rawStatisticsRepository.persist(rawStatistics);
    }

}
