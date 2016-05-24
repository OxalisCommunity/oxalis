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

package eu.peppol.as2;

import com.google.inject.Inject;
import eu.peppol.MessageDigestResult;
import eu.peppol.security.KeystoreManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 15:14
 */
@Guice(modules = {As2TestModule.class})
@Test(groups = "integration")
public class MdnMimeMessageFactoryTest {

    private MdnData mdnData;
    private MdnMimeMessageFactory mdnMimeMessageFactory;

    @Inject
    KeystoreManager keystoreManager;

    @BeforeMethod
    public void setUp() throws Exception {
        MdnData.Builder builder = new MdnData.Builder();
        mdnData = builder.subject("Sample MDN")
                .as2From("AP_000001")
                .as2To("AP_000002")
                .disposition(As2Disposition.failed("Unknown recipient"))
                .date(new Date())
                .mic(new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1"))
                .build();
        mdnMimeMessageFactory = new MdnMimeMessageFactory(keystoreManager.getOurCertificate(), keystoreManager.getOurPrivateKey());
    }

    @Test
    public void testCreateMdn() throws Exception {

        MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());
        mimeMessage.writeTo(System.out);
    }

    @Test
    public void testWithPayloadDigest() throws IOException, MessagingException, NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update("The quick brown fox jumped over the lazy dog".getBytes());

        MdnData.Builder b = new MdnData.Builder();
        MdnData data = b.subject("MDN with PayloadDigest")
                .as2From("AP_00003")
                .as2To("AP_00004")
                .disposition(As2Disposition.processed())
                .date(new Date())
                .mic(new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1"))
                .originalPayloadDigest(new MessageDigestResult(messageDigest.digest(), "SHA-256"))
                .build();
        MimeMessage signedMdn = mdnMimeMessageFactory.createSignedMdn(data, new InternetHeaders());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        signedMdn.writeTo(os);

        signedMdn.writeTo(new FileOutputStream("/tmp/t2.mdn"));

        String s = new String(os.toString("UTF-8"));

        System.out.println(s);
        assertTrue(s.contains(MdnMimeMessageFactory.X_ORIGINAL_MESSAGE_ALG), MdnMimeMessageFactory.X_ORIGINAL_MESSAGE_ALG + " not found in message");
        assertTrue(s.contains(MdnMimeMessageFactory.X_ORIGINAL_MESSAGE_DIGEST));

    }

    @Test
    public void dumpMdnAsText() throws Exception {
        MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());

        String mdnAsText = mdnMimeMessageFactory.toString(mimeMessage);
        assertTrue(mdnAsText.contains("Unknown recipient"))   ;
    }

    @Test
    public void verifyContentsOfHumanReadablePart() throws Exception {
        MimeMessage mimeMessage = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());

        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);

        // Outermost multipart/signed
        MimeMultipart multiPartSigned = mdnMimeMessageInspector.getSignedMultiPart();
        assertTrue(multiPartSigned.getContentType().contains("multipart/signed"));

        // First body part in multipart/report contains the plain text
        BodyPart textPlainBodyPart = mdnMimeMessageInspector.getPlainTextBodyPart();

        String plainText = mdnMimeMessageInspector.getPlainTextPartAsText();

        String errorMessage = mdnData.getAs2Disposition().getDispositionModifier().getDispositionModifierExtension();
        assertTrue(plainText.contains(errorMessage), "Invalid contents: " + plainText + ". <<< Expected error message: " + errorMessage);
    }

    @Test
    public void verifyTextOfHumanReadablePartWhenProcessingError() throws Exception {
        MdnData.Builder builder = new MdnData.Builder();
        String errorMessage = "AS2-To header equals AS2-From header";

        mdnData = builder.subject("Sample MDN")
                .as2From("AP_000001")
                .as2To("AP_000002")
                .disposition(As2Disposition.processedWithError(errorMessage))
                .date(new Date())
                .mic(new Mic("eeWNkOTx7yJYr2EW8CR85I7QJQY=", "sha1"))
                .build();
        MimeMessage mdn = mdnMimeMessageFactory.createSignedMdn(mdnData, new InternetHeaders());
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mdn);
        assertTrue(mdnMimeMessageInspector.getPlainTextPartAsText().contains(errorMessage), "The plain text does not contain '" + errorMessage + "'");
    }

}
