package no.difi.oxalis.commons.evidence;

import com.google.inject.Inject;
import eu.peppol.util.OxalisVersion;
import no.difi.oxalis.api.evidence.EvidenceFactory;
import no.difi.oxalis.api.lang.EvidenceException;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.transmission.TransmissionResult;
import no.difi.vefa.peppol.common.model.InstanceIdentifier;
import no.difi.vefa.peppol.evidence.jaxb.receipt.TransmissionRole;
import no.difi.vefa.peppol.evidence.lang.RemEvidenceException;
import no.difi.vefa.peppol.evidence.rem.EventCode;
import no.difi.vefa.peppol.evidence.rem.Evidence;
import no.difi.vefa.peppol.evidence.rem.EvidenceTypeInstance;
import no.difi.vefa.peppol.evidence.rem.SignedEvidenceWriter;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

import java.io.OutputStream;
import java.security.KeyStore;

/**
 * @author erlend
 * @since 4.0.0
 */
public class RemEvidenceFactory implements EvidenceFactory {

    private static final String ISSUER = String.format("Oxalis %s", OxalisVersion.getVersion());

    private final KeyStore.PrivateKeyEntry privateKeyEntry;

    @Inject
    public RemEvidenceFactory(KeyStore.PrivateKeyEntry privateKeyEntry) {
        this.privateKeyEntry = privateKeyEntry;
    }

    @Override
    public void write(OutputStream outputStream, TransmissionResult transmissionResult) throws EvidenceException {
        try {
            Evidence evidence = Evidence.newInstance()
                    .type(EvidenceTypeInstance.DELIVERY_NON_DELIVERY_TO_RECIPIENT)
                    .eventCode(EventCode.DELIVERY)
                            // Missing optional "EventReason"
                    .issuer(ISSUER)
                    .evidenceIdentifier(InstanceIdentifier.generateUUID())
                    .timestamp(transmissionResult.getTimestamp())
                    .header(transmissionResult.getHeader())
                            // Missing optional "IssuerPolicy"
                    .digest(transmissionResult.getDigest())
                    .messageIdentifier(transmissionResult.getMessageId().toVefa())
                    .transportProtocol(transmissionResult.getTransportProtocol())
                    .transmissionRole(transmissionResult instanceof TransmissionResponse ?
                            TransmissionRole.C_2 : TransmissionRole.C_3)
                    .originalReceipts(transmissionResult.getReceipts());

            SignedEvidenceWriter.write(outputStream, privateKeyEntry, evidence);
        } catch (RemEvidenceException | PeppolSecurityException e) {
            throw new EvidenceException(e.getMessage(), e);
        }
    }
}
