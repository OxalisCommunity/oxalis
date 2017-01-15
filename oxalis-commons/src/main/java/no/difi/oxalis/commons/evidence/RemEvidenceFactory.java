package no.difi.oxalis.commons.evidence;

import com.google.inject.Inject;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.vefa.peppol.evidence.jaxb.receipt.TransmissionRole;
import no.difi.vefa.peppol.evidence.lang.RemEvidenceException;
import no.difi.vefa.peppol.evidence.rem.EventCode;
import no.difi.vefa.peppol.evidence.rem.Evidence;
import no.difi.vefa.peppol.evidence.rem.EvidenceTypeInstance;
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
                    .type(EvidenceTypeInstance.DELIVERY_NON_DELIVERY_TO_RECIPIENT)
                    .eventCode(EventCode.DELIVERY)
                    .documentIdentifier(transmissionResponse.getHeader().getIdentifier())
                    .transmissionRole(TransmissionRole.C_2)
                    .sender(transmissionResponse.getHeader().getSender())
                    .receiver(transmissionResponse.getHeader().getReceiver())
                    .documentIdentifier(transmissionResponse.getHeader().getIdentifier())
                    .documentTypeIdentifier(transmissionResponse.getHeader().getDocumentType())
                    .messageIdentifier(transmissionResponse.getMessageId().toVefa());

            SignedEvidenceWriter.write(outputStream, privateKeyEntry, evidence);
        } catch (RemEvidenceException | PeppolSecurityException e) {
            throw new EvidenceException(e.getMessage(), e);
        }
    }
}
