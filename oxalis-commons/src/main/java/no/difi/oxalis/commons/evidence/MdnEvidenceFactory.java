package no.difi.oxalis.commons.evidence;

import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.transmission.TransmissionResult;

import java.io.OutputStream;

/**
 * @author erlend
 * @since 4.0.0
 */
public class MdnEvidenceFactory implements EvidenceFactory {

    @Override
    public void write(OutputStream outputStream, TransmissionResult transmissionResult) throws EvidenceException {

    }
}
