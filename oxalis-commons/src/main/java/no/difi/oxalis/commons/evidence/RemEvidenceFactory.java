package no.difi.oxalis.commons.evidence;

import com.google.inject.Inject;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.evidence.jaxb.receipt.TransmissionRole;
import no.difi.vefa.peppol.evidence.lang.RemEvidenceException;
import no.difi.vefa.peppol.evidence.rem.Evidence;
import no.difi.vefa.peppol.evidence.rem.SignedEvidenceWriter;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

import java.io.OutputStream;
import java.security.KeyStore;

public class RemEvidenceFactory implements EvidenceFactory {

    private KeyStore.PrivateKeyEntry privateKeyEntry;

    @Inject
    public RemEvidenceFactory(KeyStore.PrivateKeyEntry privateKeyEntry) {
        this.privateKeyEntry = privateKeyEntry;
    }

    @Override
    public void write(OutputStream outputStream, TransmissionResponse transmissionResponse) throws EvidenceException {
        try {
            Evidence evidence = Evidence.newInstance()
                    .documentIdentifier(transmissionResponse.getHeader().getIdentifier())
                    .transmissionRole(TransmissionRole.C_2);

            SignedEvidenceWriter.write(outputStream, privateKeyEntry, evidence);
        } catch (RemEvidenceException | PeppolSecurityException e) {
            throw new EvidenceException(e.getMessage(), e);
        }
    }
}
