package no.difi.oxalis.api.evidence;

import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.outbound.TransmissionResponse;

import java.io.OutputStream;

/**
 * @author erlend
 * @since 4.0.0
 */
public interface EvidenceFactory {

    void write(OutputStream outputStream, TransmissionResponse transmissionResponse) throws EvidenceException;
}
