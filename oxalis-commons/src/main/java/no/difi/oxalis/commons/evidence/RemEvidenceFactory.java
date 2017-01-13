package no.difi.oxalis.commons.evidence;

import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.evidence.rem.Evidence;
import no.difi.vefa.peppol.evidence.rem.SignedEvidenceWriter;

import java.io.OutputStream;
import java.security.KeyStore;

public class RemEvidenceFactory implements EvidenceFactory {

    private KeyStore.PrivateKeyEntry privateKeyEntry;

    public RemEvidenceFactory(KeyStore.PrivateKeyEntry privateKeyEntry) {
        this.privateKeyEntry = privateKeyEntry;
    }

    public void write(OutputStream outputStream, TransmissionResponse transmissionResponse) throws Exception {
        Evidence evidence = Evidence.newInstance();

        SignedEvidenceWriter.write(outputStream, privateKeyEntry, evidence);
    }
}
