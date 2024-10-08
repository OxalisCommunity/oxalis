/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.outbound.transmission;

import com.google.inject.Inject;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import network.oxalis.api.error.ErrorTracker;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.lookup.LookupService;
import network.oxalis.api.model.Direction;
import network.oxalis.api.outbound.MessageSender;
import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.api.outbound.Transmitter;
import network.oxalis.api.statistics.StatisticsService;
import network.oxalis.api.transmission.TransmissionVerifier;
import network.oxalis.commons.mode.OxalisCertificateValidator;
import network.oxalis.commons.tracing.Traceable;
import network.oxalis.vefa.peppol.common.code.Service;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.TransportProfile;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;

/**
 * Executes transmission requests by sending the payload to the requested destination.
 * Updates statistics for the transmission using the configured RawStatisticsRepository.
 * <p>
 * Will log an error if the recording of statistics fails for some reason.
 *
 * @author steinar
 * @author thore
 * @author erlend
 */
class DefaultTransmitter extends Traceable implements Transmitter {

    /**
     * Factory used to fetch implementation of required transport profile implementation.
     */
    private final MessageSenderFactory messageSenderFactory;

    /**
     * Service to report statistics when transmission is successfully transmitted.
     */
    private final StatisticsService statisticsService;

    private final TransmissionVerifier transmissionVerifier;

    private final LookupService lookupService;

    private final OxalisCertificateValidator certificateValidator;

    private final ErrorTracker errorTracker;

    @Inject
    public DefaultTransmitter(MessageSenderFactory messageSenderFactory, StatisticsService statisticsService,
                              TransmissionVerifier transmissionVerifier, LookupService lookupService, Tracer tracer,
                              OxalisCertificateValidator certificateValidator, ErrorTracker errorTracker) {
        super(tracer);
        this.messageSenderFactory = messageSenderFactory;
        this.statisticsService = statisticsService;
        this.transmissionVerifier = transmissionVerifier;
        this.lookupService = lookupService;
        this.certificateValidator = certificateValidator;
        this.errorTracker = errorTracker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransmissionResponse transmit(TransmissionMessage transmissionMessage) throws OxalisTransmissionException {
        Span span = tracer.spanBuilder("transmit").startSpan();
        try {
            return perform(transmissionMessage);
        } finally {
            span.end();
        }
    }

    private TransmissionResponse perform(TransmissionMessage transmissionMessage)
            throws OxalisTransmissionException {
        try {
            if (transmissionMessage == null)
                throw new OxalisTransmissionException("No transmission is provided.");

            transmissionVerifier.verify(transmissionMessage.getHeader(), Direction.OUT);

            TransmissionRequest transmissionRequest;
            if (transmissionMessage instanceof TransmissionRequest) {
                transmissionRequest = (TransmissionRequest) transmissionMessage;

                // Validate provided certificate
                if (transmissionRequest.getEndpoint().getCertificate() == null)
                    throw new OxalisTransmissionException("Certificate of receiving access point is not provided.");
                certificateValidator.validate(Service.AP, transmissionRequest.getEndpoint().getCertificate());
            } else {
                transmissionRequest = performLookupUserHeaders(transmissionMessage);
            }

            TransmissionResponse transmissionResponse = sendMessage(transmissionRequest);

            statisticsService.persist(transmissionRequest, transmissionResponse);

            return transmissionResponse;
        } catch (PeppolSecurityException e) {
            errorTracker.track(Direction.OUT, e, true);
            throw new OxalisTransmissionException("Unable to verify certificate of receiving access point.", e);
        } catch (OxalisTransmissionException e) {
            errorTracker.track(Direction.OUT, e, true);
            throw e;
        } catch (RuntimeException e) {
            errorTracker.track(Direction.OUT, e, false);
            throw e;
        }
    }

    private TransmissionRequest performLookupUserHeaders(TransmissionMessage transmissionMessage) throws OxalisTransmissionException {
        TransmissionRequest transmissionRequest;
        Span span = tracer.spanBuilder("Fetch endpoint information").startSpan();
        Endpoint endpoint;
        try {
            endpoint = lookupService.lookup(transmissionMessage.getHeader());
            span.setAttribute("transport profile", endpoint.getTransportProfile().getIdentifier());
            transmissionRequest = new DefaultTransmissionRequest(transmissionMessage, endpoint);
        } catch (OxalisTransmissionException e) {
            span.setAttribute("exception", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
        return transmissionRequest;
    }

    private TransmissionResponse sendMessage(TransmissionRequest transmissionRequest) throws OxalisTransmissionException {
        Span span = tracer.spanBuilder("send message").startSpan();
        TransmissionResponse transmissionResponse;
        try {
            TransportProfile transportProfile = transmissionRequest.getEndpoint().getTransportProfile();
            MessageSender messageSender = messageSenderFactory.getMessageSender(transportProfile);
            transmissionResponse = messageSender.send(transmissionRequest);
        } catch (OxalisTransmissionException e) {
            span.setAttribute("exception", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
        return transmissionResponse;
    }

}
