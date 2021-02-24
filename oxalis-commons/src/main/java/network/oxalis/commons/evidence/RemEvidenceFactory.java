/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.commons.evidence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import network.oxalis.api.evidence.EvidenceFactory;
import network.oxalis.api.lang.EvidenceException;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.api.transmission.TransmissionResult;
import network.oxalis.api.util.Type;
import network.oxalis.commons.util.OxalisVersion;
import network.oxalis.vefa.peppol.common.model.InstanceIdentifier;
import network.oxalis.vefa.peppol.evidence.jaxb.receipt.TransmissionRole;
import network.oxalis.vefa.peppol.evidence.lang.RemEvidenceException;
import network.oxalis.vefa.peppol.evidence.rem.EventCode;
import network.oxalis.vefa.peppol.evidence.rem.Evidence;
import network.oxalis.vefa.peppol.evidence.rem.EvidenceTypeInstance;
import network.oxalis.vefa.peppol.evidence.rem.SignedEvidenceWriter;
import network.oxalis.vefa.peppol.security.lang.PeppolSecurityException;

import java.io.OutputStream;
import java.security.KeyStore;

/**
 * @author erlend
 * @since 4.0.0
 */
@Singleton
@Type("rem")
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
                    .messageIdentifier(transmissionResult.getTransmissionIdentifier())
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
