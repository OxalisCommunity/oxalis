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
import eu.peppol.lang.OxalisTransmissionException;
import eu.peppol.outbound.statistics.StatisticsService;
import no.difi.oxalis.api.outbound.MessageSender;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.vefa.peppol.common.model.TransportProfile;

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

    private final MessageSenderFactory messageSenderFactory;

    private final StatisticsService statisticsService;

    private final Tracer tracer;

    @Inject
    public DefaultTransmitter(MessageSenderFactory messageSenderFactory, StatisticsService statisticsService, Tracer tracer) {
        this.messageSenderFactory = messageSenderFactory;
        this.statisticsService = statisticsService;
        this.tracer = tracer;
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
                TransportProfile transportProfile = transmissionRequest.getEndpoint().getTransportProfile();
                MessageSender messageSender = messageSenderFactory.getMessageSender(transportProfile);
                transmissionResponse = messageSender.send(transmissionRequest, span);
            } catch (OxalisTransmissionException e) {
                span.tag("exception", e.getMessage());
                throw e;
            }
        }

        statisticsService.persist(transmissionRequest, transmissionResponse, root);

        return transmissionResponse;
    }
}
