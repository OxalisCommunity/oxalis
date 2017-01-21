package no.difi.oxalis.api.evidence;

import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.transmission.TransmissionResult;

import java.io.OutputStream;

/**
 * @author erlend
 * @since 4.0.0
 */
@FunctionalInterface
public interface EvidenceFactory {

    void write(OutputStream outputStream, TransmissionResult transmissionResult) throws EvidenceException;
}
