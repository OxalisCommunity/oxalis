package no.difi.oxalis.commons.statistics;

import brave.Span;
import brave.Tracer;
import com.google.inject.Inject;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.commons.security.CertificateUtils;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.oxalis.commons.tracing.Traceable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.Date;

class DefaultStatisticsService extends Traceable implements StatisticsService {

    private static Logger logger = LoggerFactory.getLogger(DefaultStatisticsService.class);

    private final RawStatisticsRepository rawStatisticsRepository;

    private final AccessPointIdentifier ourAccessPointIdentifier;

    @Inject
    public DefaultStatisticsService(RawStatisticsRepository rawStatisticsRepository, X509Certificate certificate, Tracer tracer) {
        super(tracer);
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.ourAccessPointIdentifier = new AccessPointIdentifier(CertificateUtils.extractCommonName(certificate));
    }

    @Override
    public void persist(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse, Span root) {
        try (Span span = tracer.newChild(root.context()).name("persist statistics").start()) {
            try {
                RawStatistics.RawStatisticsBuilder builder = new RawStatistics.RawStatisticsBuilder()
                        .accessPointIdentifier(ourAccessPointIdentifier)
                        .outbound()
                        .documentType(PeppolDocumentTypeId.valueOf(transmissionRequest.getHeader().getDocumentType().getIdentifier()))
                        .sender(ParticipantId.valueOf(transmissionRequest.getHeader().getSender().getIdentifier()))
                        .receiver(ParticipantId.valueOf(transmissionRequest.getHeader().getReceiver().getIdentifier()))
                        .profile(PeppolProcessTypeId.valueOf(transmissionRequest.getHeader().getProcess().getIdentifier()))
                        .date(new Date());  // Time stamp of reception of the receipt

                // If we know the CN name of the destination AP, supply that as the channel id otherwise use the protocol name
                if (transmissionRequest.getEndpoint().getCertificate() != null) {
                    String accessPointIdentifierValue = CertificateUtils.extractCommonName(transmissionRequest.getEndpoint().getCertificate());
                    builder.channel(new ChannelId(accessPointIdentifierValue));
                } else {
                    String protocolName = transmissionRequest.getEndpoint().getTransportProfile().getValue();
                    builder.channel(new ChannelId(protocolName));
                }

                RawStatistics rawStatistics = builder.build();
                rawStatisticsRepository.persist(rawStatistics);

            } catch (Exception ex) {
                span.tag("exception", String.valueOf(ex.getMessage()));
                logger.error("Persisting RawStatistics about oubound transmission failed : {}", ex.getMessage(), ex);
            }
        }
    }

}
