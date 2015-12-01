/*
 * Copyright (c) 2015 Steinar Overbeck Cook
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.as2.evidence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.as2.As2ReceiptData;
import eu.peppol.persistence.TransmissionEvidence;
import eu.peppol.xsd.ticc.receipt._1.TransmissionRole;
import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.InstanceIdentifier;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.TransportProfile;
import no.difi.vefa.peppol.evidence.rem.EventCode;
import no.difi.vefa.peppol.evidence.rem.RemEvidenceBuilder;
import no.difi.vefa.peppol.evidence.rem.RemEvidenceService;
import no.difi.vefa.peppol.evidence.rem.SignedRemEvidence;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Creates instances of TransmissionEvidence based upon the information available when using the AS2 protocol with MDN contained in S/MIME.
 *
 * Please use the Google Guice TransportEvidenceModule for instantiation.
 *
 * @author steinar
 *         Date: 17.11.2015
 *         Time: 14.57
 */
public class As2TransmissionEvidenceFactory {

    private final RemEvidenceService remEvidenceService;
    private final KeyStore.PrivateKeyEntry privateKeyEntry;

    @Inject
    As2TransmissionEvidenceFactory(RemEvidenceService remEvidenceService,
                                   @Named("OurPrivateKey")PrivateKey ourPrivateKey,
                                   @Named("OurCertificate")X509Certificate ourCertificate) {
        this.remEvidenceService = remEvidenceService;
        privateKeyEntry = new KeyStore.PrivateKeyEntry(ourPrivateKey, new Certificate[]{ourCertificate});
    }


    /**
     * Creates TransmissionEvidence based upon the AS2 MDN and other associated data in addition to the actual S/MIME message, which was
     * returned to the sender of the original message.
     *
     * @param as2ReceiptData holds the AS2 MDN data (not the actual MDN) and the PEPPOL meta data
     * @param sMimeMesssageHoldingMdn the S/MIME message returned to the sender of the original BIS message
     * @param transmissionRole
     * @return instance of the generic TransmissionEvidence
     */
    public TransmissionEvidence createRemWithMdnEvidence(As2ReceiptData as2ReceiptData, MimeMessage sMimeMesssageHoldingMdn, TransmissionRole transmissionRole) {

        if (remEvidenceService == null || privateKeyEntry == null) {
            throw new IllegalStateException("Seems this factory was not properly initialized.");
        }

        if (sMimeMesssageHoldingMdn == null) {
            throw new NullPointerException("Argument sMimeMesssageHoldingMdn is required()");
        }
        if (as2ReceiptData == null) {
            throw new NullPointerException("Argument holding the As2ReciptData is required()");
        } else {
            if (as2ReceiptData.getMdnData() == null) {
                throw new NullPointerException("as2ReceiptData.getMdnData()");
            } else {
                if (as2ReceiptData.getMdnData().getOriginalPayloadDigest() == null) {
                    throw new NullPointerException("as2ReceiptData.getMdnData().getOriginalPayloadDigest() is required");
                }
            }
        }

        byte[] smimeToBytes = convertSmimeToBytes(sMimeMesssageHoldingMdn);

        RemEvidenceBuilder remEvidenceBuilder = remEvidenceService.createRelayRemMdAcceptanceRejectionBuilder();

        // Transforms our Oxalis representation of relevant BusDox identifiers into the genric ones
        if (as2ReceiptData.getPeppolMessageMetaData() == null) {
            throw new NullPointerException("as2ReceiptData.getPeppolMessageMetaData()");
        } else {
            if (as2ReceiptData.getPeppolMessageMetaData().getRecipientId() == null) {
                throw new NullPointerException("as2ReceiptData.getPeppolMessageMetaData().getRecipientId()");
            }
            if (as2ReceiptData.getPeppolMessageMetaData().getSenderId() == null) {
                throw new NullPointerException("as2ReceiptData.getPeppolMessageMetaData().getSenderId()");
            }
            if (as2ReceiptData.getPeppolMessageMetaData().getDocumentTypeIdentifier() == null) {
                throw new NullPointerException("as2ReceiptData.getPeppolMessageMetaData().getDocumentTypeIdentifier()");
            }
            if (as2ReceiptData.getPeppolMessageMetaData().getTransmissionId() == null) {
                throw new NullPointerException("as2ReceiptData.getPeppolMessageMetaData().getTransmissionId()");
            }
        }
        ParticipantIdentifier recipientId = new ParticipantIdentifier(as2ReceiptData.getPeppolMessageMetaData().getRecipientId().stringValue());
        ParticipantIdentifier senderId = new ParticipantIdentifier(as2ReceiptData.getPeppolMessageMetaData().getSenderId().stringValue());
        DocumentTypeIdentifier documentTypeId = new DocumentTypeIdentifier(as2ReceiptData.getPeppolMessageMetaData().getDocumentTypeIdentifier().toString());
        Date receptionTimeStamp = as2ReceiptData.getMdnData().getReceptionTimeStamp();


        remEvidenceBuilder
                .eventCode(EventCode.ACCEPTANCE)
                // time stamp from the MDN
                .eventTime(receptionTimeStamp)
                // The sender of the BIS message
                .senderIdentifier(senderId)
                // The receiver of the BIS message
                .recipientIdentifer(recipientId)
                // The document type identificator (BIS doc. type id)
                .documentTypeId(documentTypeId)
                // From the SBDH: //DocumentIdentification/InstanceIdentifier
                .instanceIdentifier(new InstanceIdentifier(as2ReceiptData.getPeppolMessageMetaData().getTransmissionId().toString()))
                // Digest of the original payload
                .payloadDigest(as2ReceiptData.getMdnData().getOriginalPayloadDigest().getDigest())
                // The bytes of the S/MIME message holding the signed MDN
                .protocolSpecificEvidence(transmissionRole, TransportProfile.AS2_1_0, smimeToBytes)
        ;

        // Signs and builds the REMEvidenceType with the S/MIME holding the MDN, included in the Extensions section of the REM
        SignedRemEvidence signedRemEvidence = remEvidenceBuilder.buildRemEvidenceInstance(privateKeyEntry);

        // Finally wrap it inside something that realizes the TransmissionEvidence interface
        As2RemWithMdnTransmissionEvidenceImpl as2RemWithMdnTransmissionEvidence = new As2RemWithMdnTransmissionEvidenceImpl(signedRemEvidence, as2ReceiptData, sMimeMesssageHoldingMdn);
        return as2RemWithMdnTransmissionEvidence;
    }


    private byte[] convertSmimeToBytes(MimeMessage mimeMessage) {
        ByteArrayOutputStream evidenceBytes = new ByteArrayOutputStream();
        try {
            mimeMessage.writeTo(evidenceBytes);
        } catch (IOException | MessagingException e) {
            throw new IllegalStateException("Unable to convert MDN mime message into bytes()");
        }
        return evidenceBytes.toByteArray();
    }
}
