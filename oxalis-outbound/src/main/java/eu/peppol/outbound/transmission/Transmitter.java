package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.security.CommonName;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Executes transmission requests by sending the payload to the requested destination.
 * Updates statistics about the transmission using the configured RawStatisticsRepository.
 *
 * Will log an error if the recording of statistics fails for some reason.
 *
 * @author steinar
 * @author thore
 */
public class Transmitter {

    public static final Logger log = LoggerFactory.getLogger(Transmitter.class);

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

    /**
     * Tries to update the raw statistics with information about the transmission.
     * If persisting statistics fails for some reason, we will allowed the transmission to
     * return successfully (since message has been accepted by the received), but we will
     * log an error.
     *
     * @param transmissionRequest
     * @param transmissionResponse
     */
    void persistStatistics(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse) {

        try {

            RawStatistics.RawStatisticsBuilder builder = new RawStatistics.RawStatisticsBuilder()
                    .accessPointIdentifier(ourAccessPointIdentifier)
                    .outbound()
                    .documentType(transmissionRequest.getPeppolStandardBusinessHeader().getDocumentTypeIdentifier())
                    .sender(transmissionRequest.getPeppolStandardBusinessHeader().getSenderId())
                    .receiver(transmissionRequest.getPeppolStandardBusinessHeader().getRecipientId())
                    .profile(transmissionRequest.getPeppolStandardBusinessHeader().getProfileTypeIdentifier())
                    .date(new Date());  // Time stamp of reception

            // If we know the CN name of the destination AP, supply that as the channel id otherwise use the protocol name
            if (transmissionRequest.getEndpointAddress().getCommonName() != null) {
                String accessPointIdentifierValue = transmissionRequest.getEndpointAddress().getCommonName().toString();
                builder.channel(new ChannelId(accessPointIdentifierValue));
            } else {
                String protocolName = transmissionRequest.getEndpointAddress().getBusDoxProtocol().name();
                builder.channel(new ChannelId(protocolName));
            }

            RawStatistics rawStatistics = builder.build();
            rawStatisticsRepository.persist(rawStatistics);

        } catch (Exception ex) {
            log.error("Persisting RawStatistics about oubound transmission failed : " + ex.getMessage(), ex);
        }

    }

}
