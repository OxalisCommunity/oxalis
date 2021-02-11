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

package network.oxalis.statistics.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.inbound.InboundMetadata;
import network.oxalis.api.model.AccessPointIdentifier;
import network.oxalis.api.model.Direction;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.api.statistics.StatisticsService;
import network.oxalis.api.util.Type;
import network.oxalis.commons.security.CertificateUtils;
import network.oxalis.commons.tracing.Traceable;
import network.oxalis.statistics.api.ChannelId;
import network.oxalis.statistics.api.RawStatisticsRepository;
import network.oxalis.statistics.model.DefaultRawStatistics;

import java.security.cert.X509Certificate;

@Slf4j
@Singleton
@Type("default")
class DefaultStatisticsService extends Traceable implements StatisticsService {

    private final RawStatisticsRepository rawStatisticsRepository;

    private final AccessPointIdentifier ourAccessPointIdentifier;

    @Inject
    public DefaultStatisticsService(RawStatisticsRepository rawStatisticsRepository,
                                    X509Certificate certificate, Tracer tracer) {
        super(tracer);
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.ourAccessPointIdentifier = new AccessPointIdentifier(CertificateUtils.extractCommonName(certificate));
    }

    @Override
    public void persist(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse, Span root) {
        Span span = tracer.buildSpan("persist statistics").asChildOf(root).start();
        try {
            DefaultRawStatistics.RawStatisticsBuilder builder = new DefaultRawStatistics.RawStatisticsBuilder()
                    .accessPointIdentifier(ourAccessPointIdentifier)
                    .direction(Direction.OUT)
                    .documentType(transmissionResponse.getHeader().getDocumentType())
                    .sender(transmissionResponse.getHeader().getSender())
                    .receiver(transmissionResponse.getHeader().getReceiver())
                    .profile(transmissionResponse.getHeader().getProcess())
                    .date(transmissionResponse.getTimestamp());  // Time stamp of reception of the receipt

            // If we know the CN name of the destination AP, supply that
            // as the channel id otherwise use the protocol name
            if (transmissionRequest.getEndpoint().getCertificate() != null) {
                String accessPointIdentifierValue = CertificateUtils
                        .extractCommonName(transmissionRequest.getEndpoint().getCertificate());
                builder.channel(new ChannelId(accessPointIdentifierValue));
            } else {
                String protocolName = transmissionRequest.getEndpoint().getTransportProfile().getIdentifier();
                builder.channel(new ChannelId(protocolName));
            }

            DefaultRawStatistics rawStatistics = builder.build();
            rawStatisticsRepository.persist(rawStatistics);
        } catch (Exception ex) {
            span.setTag("exception", String.valueOf(ex.getMessage()));
            log.error("Persisting DefaultRawStatistics about oubound transmission failed : {}", ex.getMessage(), ex);
        } finally {
            span.finish();
        }
    }

    public void persist(InboundMetadata inboundMetadata) {
        // Persists raw statistics when message was received (ignore if stats couldn't be persisted, just warn)
        try {
            DefaultRawStatistics rawStatistics = new DefaultRawStatistics.RawStatisticsBuilder()
                    .accessPointIdentifier(ourAccessPointIdentifier)
                    .direction(Direction.IN)
                    .documentType(inboundMetadata.getHeader().getDocumentType())
                    .sender(inboundMetadata.getHeader().getSender())
                    .receiver(inboundMetadata.getHeader().getReceiver())
                    .profile(inboundMetadata.getHeader().getProcess())
                    .channel(new ChannelId("AS2"))
                    .build();

            rawStatisticsRepository.persist(rawStatistics);
        } catch (Exception e) {
            log.error("Unable to persist statistics for " + inboundMetadata.toString() + ";\n " + e.getMessage(), e);
        }
    }
}
