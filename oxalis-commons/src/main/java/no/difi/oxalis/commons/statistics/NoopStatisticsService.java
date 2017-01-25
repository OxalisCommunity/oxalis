package no.difi.oxalis.commons.statistics;

import brave.Span;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.statistics.StatisticsService;

/**
 * NOOP implementation of {@link StatisticsService}.
 */
class NoopStatisticsService implements StatisticsService {

    @Override
    public void persist(TransmissionRequest transmissionRequest,
                        TransmissionResponse transmissionResponse, Span root) {
        // No action.
    }

    @Override
    public void persist(InboundMetadata inboundMetadata) {
        // No action.
    }
}
