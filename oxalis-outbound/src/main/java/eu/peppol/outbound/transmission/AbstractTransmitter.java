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

package eu.peppol.outbound.transmission;

import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.outbound.api.MessageSender;
import eu.peppol.outbound.api.TransmissionResponse;
import eu.peppol.outbound.api.Transmitter;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author steinar
 *         Date: 18.11.2016
 *         Time: 16.28
 */
abstract class AbstractTransmitter implements Transmitter {

    private static final Logger log = LoggerFactory.getLogger(AbstractTransmitter.class);

    protected final MessageSenderFactory messageSenderFactory;

    protected final RawStatisticsRepository rawStatisticsRepository;

    protected final CommonName ourCommonName;

    protected final KeystoreManager keystoreManager;

    protected AccessPointIdentifier ourAccessPointIdentifier;

    AbstractTransmitter(MessageSenderFactory messageSenderFactory, RawStatisticsRepository rawStatisticsRepository, KeystoreManager keystoreManager) {
        this.messageSenderFactory = messageSenderFactory;
        this.ourCommonName = keystoreManager.getOurCommonName();
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.keystoreManager = keystoreManager;
        ourAccessPointIdentifier = new AccessPointIdentifier(ourCommonName.toString());

        if (ourCommonName == null) {
            throw new IllegalArgumentException("Must supply the Common Name (CN) for our access point");
        }
    }

    @Override
    public TransmissionResponse transmit(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        BusDoxProtocol busDoxProtocol = transmissionRequest.getEndpointAddress().getBusDoxProtocol();
        MessageSender messageSender = messageSenderFactory.createMessageSender(busDoxProtocol);
        TransmissionResponse transmissionResponse = messageSender.send(transmissionRequest);

        persistTransmissionResponse(transmissionRequest, transmissionResponse);


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
    protected void persistTransmissionResponse(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse) {

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
