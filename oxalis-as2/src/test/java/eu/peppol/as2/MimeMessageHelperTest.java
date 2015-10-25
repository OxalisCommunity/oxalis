/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
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

package eu.peppol.as2;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JcaX509CertSelectorConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESigned;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import org.bouncycastle.mail.smime.validator.SignedMailValidator;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.*;

/**
 * @author steinar
 * @author thore
 */
public class MimeMessageHelperTest {

    public static final String OPENAS2_MDN_TXT = "openas2-mdn.txt";
    public static final String OPENAS2_MDN_NO_HEADERS_TXT = "openas2-mdn-no-headers.txt";

    @Test
    public void parseLegalMimeMessageWithHeaders() throws Exception {

        InputStream resourceAsStream = MimeMessageHelperTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_TXT);
        assertNotNull(resourceAsStream, OPENAS2_MDN_TXT + " not found in classpath");

        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessage(resourceAsStream);
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
    public void verifyingSignatureOfRealMdn() throws Exception {

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
        String resourcePath = "real-mdn-examples/" + resourceName;

        try {

            // add provider
            Security.addProvider(new BouncyCastleProvider());

            // shortcuts lots of steps in the above test (parseLegalMimeMessageWithHeaders)
            MimeMultipart multipartSigned = (MimeMultipart) MimeMessageHelper.createMimeMessage(MimeMessageHelperTest.class.getClassLoader().getResourceAsStream(resourcePath)).getContent();
            assertNotNull(multipartSigned);

            // verify signature


            SMIMESigned signedMessage = new SMIMESigned(multipartSigned);
            Store certs = signedMessage.getCertificates();

            SignerInformationStore signers = signedMessage.getSignerInfos();

            for (Object signerInformation : signers.getSigners()) {
                SignerInformation signer = (SignerInformation) signerInformation;
                Collection certCollection = certs.getMatches(signer.getSID());

                Iterator certIterator = certCollection.iterator();

                X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) certIterator.next());

                if (debug) System.out.println("Signing certificate : " + cert);

                SignerInformationVerifier signerInformationVerifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert);
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
        InputStream resourceAsStream = MimeMessageHelperTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_NO_HEADERS_TXT);
        assertNotNull(resourceAsStream, OPENAS2_MDN_NO_HEADERS_TXT + " not found in classpath");

        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessage(resourceAsStream);
        Object content = mimeMessage.getContent();
        assertNotNull(content);
        assertTrue(content instanceof String);
        assertEquals(mimeMessage.getContentType(), "text/plain");
    }


    @Test
    public void parseMimeMessageExperiment() throws IOException, MessagingException, MimeTypeParseException {

        InputStream inputStream = MimeMessageHelperTest.class.getClassLoader().getResourceAsStream("mime-message.txt");
        assertNotNull(inputStream, "mime-message.txt not found in class path");


        MimeMessage mimeMessage = MimeMessageHelper.parseMultipart(inputStream, new MimeType("multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha-1; boundary=\"----=_Part_34_426016548.1445612302735\""));

        Object content = mimeMessage.getContent();
        assertTrue(content instanceof MimeMultipart);


        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mimeMessage.writeTo(os);

        String s = new String(os.toByteArray());
        assertFalse(s.contains("--null"));
    }


    /**
     * Verifies that if you supply the correct "Content-Type:" together with an input stream, which does not contain the
     * required "Content-Type:" header at the start, may be created by simply supplying the header.
     * <p>
     * This would mimic how to create a mime message from a Servlet input stream.
     *
     * @throws Exception
     */
    @Test
    public void parseMimeMessageStreamWithSuppliedContentType() throws Exception {
        InputStream resourceAsStream = MimeMessageHelperTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_NO_HEADERS_TXT);
        assertNotNull(resourceAsStream, OPENAS2_MDN_NO_HEADERS_TXT + " not found in classpath");

        MimeType mimeType = new MimeType("multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha1;\n" +
                "\tboundary=\"----=_Part_2_1193010873.1384331414156\"");
        assertEquals(mimeType.getBaseType(), "multipart/signed");
        assertEquals(mimeType.getSubType(), "signed");

        MimeMessage m2 = MimeMessageHelper.parseMultipart(resourceAsStream, mimeType);

        m2.writeTo(System.out);
        Object content2 = m2.getContent();
        assertTrue(content2 instanceof MimeMultipart, "Not MimeMultiPart as excpected, but " + content2.getClass().getSimpleName());
        assertEquals(new MimeType(m2.getContentType()).getBaseType(), new MimeType("multipart/signed").getBaseType());
    }

}
