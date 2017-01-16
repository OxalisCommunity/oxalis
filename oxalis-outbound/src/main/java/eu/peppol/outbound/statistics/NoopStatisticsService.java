package eu.peppol.outbound.statistics;

import brave.Span;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;

class NoopStatisticsService implements StatisticsService {

    @Override
    public void persist(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse, Span root) {
        // No action.
    }
}
