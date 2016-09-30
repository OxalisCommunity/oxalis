/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Public Government and eGovernment (Difi)
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
import eu.peppol.security.KeystoreManager;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 07.10.13
 *         Time: 21:57
 */
@Guice(modules = {As2TestModule.class})
public class As2MessageFactoryTest {


    @Inject
    KeystoreManager keystoreManager;


    /** Creates sample As2Message by using the headers stored in test resource and creating S/MIME message using
     * contents from test resources.
     *
     * @throws Exception
     */
    @Test
    public void createSampleAs2MessageFromScratch() throws Exception {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sbdh-asic.xml");
        assertNotNull(resourceAsStream);

        MimeBodyPart mimeBodyPart = MimeMessageHelper.createMimeBodyPart(resourceAsStream, new MimeType("application/xml"));
        SMimeMessageFactory sMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());
        MimeMessage mimeMessageSigned = sMimeMessageFactory.createSignedMimeMessage(mimeBodyPart);
        SignedMimeMessage signedMimeMessage = new SignedMimeMessage(mimeMessageSigned);
        signedMimeMessage.getSignersX509Certificate();

        InternetHeaders internetHeaders1 = loadSampleHeaders();
        As2Message as2Message = As2MessageFactory.createAs2MessageFrom(internetHeaders1, signedMimeMessage);

        // Grabs the MIME multipart ...
        MimeMultipart mimeMultipart = (MimeMultipart) as2Message.getSignedMimeMessage().getMimeMessage().getContent();

        // First part contains the payload
        BodyPart bodyPart = mimeMultipart.getBodyPart(0);

        // ... which should be the SBDH
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bodyPart.writeTo(byteArrayOutputStream);
        String s = byteArrayOutputStream.toString("UTF-8");
        assertTrue(s.contains("StandardBusinessDocument"));
    }

    InternetHeaders loadSampleHeaders() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sample-as2-post-headers.txt")) {
            InternetHeaders internetHeaders = new InternetHeaders(is);
            return internetHeaders;
        } catch (IOException | MessagingException e) {
            throw new IllegalStateException("Unable to load data from sample-as2-post-headers.txt", e);
        }
    }
}
