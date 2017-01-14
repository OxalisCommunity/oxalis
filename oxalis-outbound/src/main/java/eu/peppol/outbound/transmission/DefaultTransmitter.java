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

import brave.Span;
import brave.Tracer;
import com.google.inject.Inject;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.vefa.peppol.common.model.TransportProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Executes transmission requests by sending the payload to the requested destination.
 * Updates statistics for the transmission using the configured RawStatisticsRepository.
 * <p>
 * Will log an error if the recording of statistics fails for some reason.
 *
 * @author steinar
 * @author thore
 */
class DefaultTransmitter implements Transmitter {

    private static final Logger log = LoggerFactory.getLogger(DefaultTransmitter.class);

    protected final MessageSenderFactory messageSenderFactory;

    protected final RawStatisticsRepository rawStatisticsRepository;

    protected final CommonName ourCommonName;

    protected final KeystoreManager keystoreManager;

    protected final Tracer tracer;

    protected AccessPointIdentifier ourAccessPointIdentifier;

    @Inject
    public DefaultTransmitter(MessageSenderFactory messageSenderFactory, RawStatisticsRepository rawStatisticsRepository,
                              KeystoreManager keystoreManager, Tracer tracer) {
        this.messageSenderFactory = messageSenderFactory;
        this.ourCommonName = keystoreManager.getOurCommonName();
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.keystoreManager = keystoreManager;
        this.tracer = tracer;
        ourAccessPointIdentifier = new AccessPointIdentifier(ourCommonName.toString());

        if (ourCommonName == null)
            throw new IllegalArgumentException("Must supply the Common Name (CN) for our access point");
    }

    @Override
    public TransmissionResponse transmit(TransmissionRequest transmissionRequest, Span root) throws OxalisTransmissionException {
        try (Span span = tracer.newChild(root.context()).name("transmit").start()) {
            return perform(transmissionRequest, span);
        }
    }

    @Override
    public TransmissionResponse transmit(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        try (Span root = tracer.newTrace().name("transmit").start()) {
            return perform(transmissionRequest, root);
        }
    }

    private TransmissionResponse perform(TransmissionRequest transmissionRequest, Span root) throws OxalisTransmissionException {
        TransmissionResponse transmissionResponse;
        try (Span span = tracer.newChild(root.context()).name("send message").start()) {
            try {
                TransportProfile transportProfile = transmissionRequest.getEndpointAddress().getTransportProfile();
                MessageSender messageSender = messageSenderFactory.getMessageSender(transportProfile);
                transmissionResponse = messageSender.send(transmissionRequest, span);
            } catch (OxalisTransmissionException e) {
                span.tag("exception", e.getMessage());
                throw e;
            }
        }

        persistStatistics(transmissionRequest, transmissionResponse, root);

        return transmissionResponse;
    }

    /**
     * Tries to update the raw statistics with information about the transmission.
     * If persisting statistics fails for some reason, we will allowed the transmission to
     * return successfully (since message has been accepted by the received), but we will
     * log an error.
     */
    protected void persistStatistics(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse, Span root) {
        try (Span span = tracer.newChild(root.context()).name("persist statistics").start()) {
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
                    String protocolName = transmissionRequest.getEndpointAddress().getTransportProfile().getValue();
                    builder.channel(new ChannelId(protocolName));
                }

                RawStatistics rawStatistics = builder.build();
                rawStatisticsRepository.persist(rawStatistics);

            } catch (Exception ex) {
                span.tag("exception", ex.getMessage());
                log.error("Persisting RawStatistics about oubound transmission failed : {}", ex.getMessage(), ex);
            }
        }
    }
}
