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
import eu.peppol.MessageDigestResult;
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.as2.*;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.identifier.WellKnownParticipant;
import eu.peppol.persistence.TransmissionEvidence;
import eu.peppol.security.KeystoreManager;
import eu.peppol.security.SecurityModule;
import eu.peppol.xsd.ticc.receipt._1.TransmissionRole;
import no.difi.vefa.peppol.evidence.rem.RemEvidenceService;
import no.difi.vefa.peppol.security.api.PeppolSecurityException;
import no.difi.vefa.peppol.security.xmldsig.XmldsigSigner;
import no.difi.vefa.peppol.security.xmldsig.XmldsigVerifier;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.UUID;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 16.11.2015
 *         Time: 11.59
 */
@Test(groups = {"integration"})
@Guice(modules = {TransportEvidenceModule.class, SecurityModule.class})
public class As2TransmissionEvidenceFactoryIT {

    @Inject
    As2TransmissionEvidenceFactory evidenceFactory;

    @Inject
    RemEvidenceService remEvidenceService;

    @Inject
    KeystoreManager keystoreManager;

    /**
     * Attempts to create TransmissionEvidence using the As2TransmissionEvidenceFactory
     *
     * @throws Exception
     */
    @Test
    public void createTransmissionEvidenceWithRemAndMdn() throws PeppolSecurityException, ParserConfigurationException, NoSuchAlgorithmException, JAXBException {

        assertNotNull(evidenceFactory, "field evidenceFactory has not been initialized");

        TransmissionEvidence remWithMdnEvidence = null;
        remWithMdnEvidence = createSampleTransmissionEvidenceWithRemAndMdn();

        assertNotNull(remWithMdnEvidence.getReceptionTimeStamp());
        assertTrue(remWithMdnEvidence instanceof As2RemWithMdnTransmissionEvidenceImpl);

        As2RemWithMdnTransmissionEvidenceImpl rem = (As2RemWithMdnTransmissionEvidenceImpl) remWithMdnEvidence;
    }


    @Test
    public void simpleSignAndVerify() throws Exception {

        String xml = "<customer>Steinar</customer>";

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(keystoreManager.getOurPrivateKey(), new Certificate[]{keystoreManager.getOurCertificate()});
        Document signedDocument = documentBuilder.newDocument();

        DOMResult domResult = new DOMResult(signedDocument);

        XmldsigSigner.sign(document, privateKeyEntry, domResult);

        X509Certificate x509Certificate = XmldsigVerifier.verify(signedDocument);
    }


    protected TransmissionEvidence createSampleTransmissionEvidenceWithRemAndMdn() throws NoSuchAlgorithmException {
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
                .disposition(As2Disposition.failed("Unknown recipient"))
                .date(new Date())
                .mic(new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1"))
                .originalPayloadDigest(messageDigestResult)
                .build();

        // Creates a sample S/MIME message holding the signed MDN
        MdnMimeMessageFactory mdnMimeMessageFactory = new MdnMimeMessageFactory(keystoreManager.getOurCertificate(), keystoreManager.getOurPrivateKey());
        MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());

        // Creates the PeppolMessageMetaData instance to be held in the As2ReceiptData
        PeppolMessageMetaData peppolMessageMetaData = new PeppolMessageMetaData();
        peppolMessageMetaData.setRecipientId(WellKnownParticipant.DIFI_TEST);
        peppolMessageMetaData.setSenderId(WellKnownParticipant.U4_TEST);
        peppolMessageMetaData.setTransmissionId(new TransmissionId(UUID.randomUUID()));
        peppolMessageMetaData.setDocumentTypeIdentifier(PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier());

        // Creates the sample As2ReceiptData holding the meta data of the transmission
        As2ReceiptData as2ReceiptData = new As2ReceiptData(mdnData, peppolMessageMetaData);

        // Finally! we attempt to create the evidence
        return evidenceFactory.createRemWithMdnEvidence(as2ReceiptData, mimeMessage, TransmissionRole.C_3);
    }
}