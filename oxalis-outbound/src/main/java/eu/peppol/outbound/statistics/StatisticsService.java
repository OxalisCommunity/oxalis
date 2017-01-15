package eu.peppol.outbound.statistics;

import brave.Span;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;

public interface StatisticsService {

    void persist(TransmissionRequest transmissionRequest, TransmissionResponse transmissionResponse, Span root);
}
