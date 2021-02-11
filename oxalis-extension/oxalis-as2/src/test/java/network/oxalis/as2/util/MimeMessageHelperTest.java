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

package network.oxalis.as2.util;

import network.oxalis.commons.bouncycastle.BCHelper;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.util.Store;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import static org.testng.Assert.*;

/**
 * @author steinar
 * @author thore
 */
public class MimeMessageHelperTest {

    public static final String OPENAS2_MDN_TXT = "/openas2-mdn.txt";

    public static final String OPENAS2_MDN_NO_HEADERS_TXT = "/openas2-mdn-no-headers.txt";

    static {
        BCHelper.registerProvider();
    }

    @Test
    public void parseLegalMimeMessageWithHeaders() throws Exception {

        InputStream resourceAsStream = getClass().getResourceAsStream(OPENAS2_MDN_TXT);
        assertNotNull(resourceAsStream, OPENAS2_MDN_TXT + " not found in classpath");

        MimeMessage mimeMessage = MimeMessageHelper.parse(resourceAsStream);
        Object content = mimeMessage.getContent();
        assertNotNull(content);
        String contentType = mimeMessage.getContentType();
        assertEquals(new MimeType(contentType).getBaseType(), new MimeType("multipart/signed").getBaseType());

        MimeMultipart mimeMultipartSigned = (MimeMultipart) content;

        // Two Bodies in the MimeMultiPart
        assertEquals(mimeMultipartSigned.getCount(), 2);

        // First body is a multipart/report
        BodyPart firstBody = mimeMultipartSigned.getBodyPart(0);
        assertEquals(new MimeType(firstBody.getContentType()).getBaseType(), new MimeType("multipart/report").getBaseType());

        // Second body contains the signature
        BodyPart secondBody = mimeMultipartSigned.getBodyPart(1);
        assertEquals(new MimeType(secondBody.getContentType()).getBaseType(), new MimeType("application/pkcs7-signature").getBaseType());

        // The inner multipart should contain two bodies: the text/plain and the message/disposition-notification
        MimeMultipart innerMultiPart = (MimeMultipart) firstBody.getContent();
        assertNotNull(innerMultiPart);
        // Must have two parts
        assertEquals(innerMultiPart.getCount(), 2);

        // First part is text/plain
        assertEquals(new MimeType(innerMultiPart.getBodyPart(0).getContentType()).getBaseType(), new MimeType("text/plain").getBaseType());

        // Second part of first part in multipart is message/disposition-notification
        assertEquals(new MimeType(innerMultiPart.getBodyPart(1).getContentType()).getBaseType(), new MimeType("message/disposition-notification").getBaseType());
    }

    @Test
    public void verifyingSignatureOfRealMdn() {

        boolean debug = false; // enable this to add certificate debugging

        // first we validate some real positive MDN's from various systems
        String[] mdnsToVerify = {"itsligo-mdn.txt", "unit4-mdn.txt", "unimaze-mdn.txt", "difi-negative-mdn.txt"};
        for (String resourceName : mdnsToVerify) {
            boolean verified = verify(resourceName, debug);
            //System.out.println("Verification of " + resourceName + " returned status=" + verified);
            assertTrue(verified, "Resource " + resourceName + " signature did not validate");
        }

        // then we validate some real negative MDN's from various systems
        String[] mdnsNegative = {"unit4-mdn-negative.txt"};
        for (String resourceName : mdnsNegative) {
            boolean verified = verify(resourceName, debug);
            assertTrue(verified, "Resource " + resourceName + " signature did not validated");
        }

        // then we validate some corrupt MDN's we have manually messed up
        String[] mdnsToFail = {"unit4-mdn-error.txt"};
        for (String resourceName : mdnsToFail) {
            boolean failed = verify(resourceName, debug);
            assertFalse(failed, "Resource " + resourceName + " signature should not have validated");
        }

        if (debug) {
            // dump list of all providers registered
            for (Provider p : Security.getProviders()) {
                System.out.println("Provider : " + p.getName());
            }
        }

    }

    /**
     * verify the signature (assuming the cert is contained in the message)
     */
    private boolean verify(String resourceName, boolean debug) {

        System.out.println("Verifying resource " + resourceName + " (debug=" + debug + ")");
        String resourcePath = "/real-mdn-examples/" + resourceName;

        try {
            // shortcuts lots of steps in the above test (parseLegalMimeMessageWithHeaders)
            MimeMultipart multipartSigned = (MimeMultipart) MimeMessageHelper.parse(
                    getClass().getResourceAsStream(resourcePath)).getContent();
            assertNotNull(multipartSigned);

            // verify signature


            SMIMESigned signedMessage = new SMIMESigned(multipartSigned);
            Store certs = signedMessage.getCertificates();

            SignerInformationStore signers = signedMessage.getSignerInfos();

            for (Object signerInformation : signers.getSigners()) {
                SignerInformation signer = (SignerInformation) signerInformation;
                Collection certCollection = certs.getMatches(signer.getSID());

                Iterator certIterator = certCollection.iterator();

                X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate((X509CertificateHolder) certIterator.next());

                if (debug) System.out.println("Signing certificate : " + cert);

                SignerInformationVerifier signerInformationVerifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build(cert);
                if (signer.verify(signerInformationVerifier))
                    return true;

            }

        } catch (Exception ex) {
            System.out.println("Verification failed with exception " + ex.getMessage());
        }

        return false;

    }

    /**
     * Verifies that if you don't supply the headers, a plain/text message will be created even though it might appear as being a multipart.
     *
     * @throws Exception
     */
    @Test
    public void parseMimeMessageStreamWithoutContentTypeEmbedded() throws Exception {
        InputStream resourceAsStream = getClass().getResourceAsStream(OPENAS2_MDN_NO_HEADERS_TXT);
        assertNotNull(resourceAsStream, OPENAS2_MDN_NO_HEADERS_TXT + " not found in classpath");

        MimeMessage mimeMessage = MimeMessageHelper.parse(resourceAsStream);
        Object content = mimeMessage.getContent();
        assertNotNull(content);
        assertTrue(content instanceof String);
        assertEquals(mimeMessage.getContentType(), "text/plain");
    }


    @Test
    public void parseMimeMessageExperiment() throws IOException, MessagingException {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mime-message.txt");
        assertNotNull(inputStream, "mime-message.txt not found in class path");


        MimeMessage mimeMessage = MimeMessageHelper.parse(inputStream);

        Object content = mimeMessage.getContent();
        assertTrue(content instanceof MimeMultipart);


        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mimeMessage.writeTo(os);

        String s = new String(os.toByteArray());
        assertFalse(s.contains("--null"));
    }
}
