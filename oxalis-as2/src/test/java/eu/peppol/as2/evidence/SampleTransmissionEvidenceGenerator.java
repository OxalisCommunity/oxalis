/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.as2.evidence;

import com.google.inject.Inject;
import eu.peppol.MessageDigestResult;
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.as2.As2Disposition;
import eu.peppol.as2.MdnData;
import eu.peppol.as2.MdnMimeMessageFactory;
import eu.peppol.as2.Mic;
import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.security.KeystoreManager;
import eu.peppol.xsd.ticc.receipt._1.TransmissionRole;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * @author steinar
 *         Date: 20.11.2015
 *         Time: 17.04
 */
public class SampleTransmissionEvidenceGenerator {

    @Inject
    As2TransmissionEvidenceFactory evidenceFactory;

    @Inject
    KeystoreManager keystoreManager;

    TransmissionEvidence createSampleTransmissionEvidenceWithRemAndMdn() throws NoSuchAlgorithmException {
        // Creates a sample message digest of the payload dummy
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update("The quick brown fox jumped over the lazy dog!".getBytes());
        byte[] digest = messageDigest.digest();
        MessageDigestResult messageDigestResult = new MessageDigestResult(digest, "SHA-256");

        // Creates sample MdnData, which goes into the S/MIME message holding the signed MDN
        MdnData.Builder builder = new MdnData.Builder();
        MdnData mdnData = builder.subject("Sample MDN")
                .as2From("AP_000001")
                .as2To("AP_000002")
                .disposition(As2Disposition.processedWithWarning("This is just a test"))
                .date(new Date())
                .mic(new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1"))
                .originalPayloadDigest(messageDigestResult)
                .build();

        // Creates a sample S/MIME message holding the signed MDN
        MdnMimeMessageFactory mdnMimeMessageFactory = new MdnMimeMessageFactory(keystoreManager.getOurCertificate(), keystoreManager.getOurPrivateKey());
        MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());

        // Creates the PeppolMessageMetaData instance
        PeppolMessageMetaData peppolMessageMetaData = new PeppolMessageMetaData();
        peppolMessageMetaData.setRecipientId(WellKnownParticipant.DIFI_TEST);
        peppolMessageMetaData.setSenderId(WellKnownParticipant.U4_TEST);
        peppolMessageMetaData.setTransmissionId(new TransmissionId(UUID.randomUUID()));
        peppolMessageMetaData.setDocumentTypeIdentifier(PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier());

        // Finally! we attempt to create the evidence
        return evidenceFactory.createRemWithMdnEvidence(mdnData, peppolMessageMetaData, mimeMessage, TransmissionRole.C_3);
    }

}
