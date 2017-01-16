package eu.peppol.outbound.statistics;

import brave.Span;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.statistics.StatisticsService;

class NoopStatisticsService implements StatisticsService {

    @Override
    public void persist(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse, Span root) {
        // No action.
    }
}
