package no.difi.oxalis.api.statistics;

import brave.Span;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;

/**
 * @author erlend
 * @since 4.0.0
 */
public interface StatisticsService {

    void persist(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse, Span root);

    void persist(InboundMetadata inboundMetadata);
}
