/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
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

package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Executes transmission requests by sending the payload to the requested destination.
 * Updates statistics for the transmission using the configured RawStatisticsRepository.
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
    public Transmitter(MessageSenderFactory messageSenderFactory, RawStatisticsRepository rawStatisticsRepository, KeystoreManager keystoreManager) {
        this.messageSenderFactory = messageSenderFactory;
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.ourCommonName = keystoreManager.getOurCommonName();
        if (ourCommonName == null) {
            throw new IllegalArgumentException("Must supply the Common Name (CN) for our access point");
        }
        ourAccessPointIdentifier = new AccessPointIdentifier(ourCommonName.toString());
    }

    public TransmissionResponse transmit(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
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
                    .date(new Date());  // Time stamp of reception of the receipt

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
