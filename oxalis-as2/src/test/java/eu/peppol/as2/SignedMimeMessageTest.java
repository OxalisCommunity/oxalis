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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 22.10.13
 *         Time: 16:13
 */
@Test(groups = "integration")
@Guice(modules = {As2TestModule.class})
public class SignedMimeMessageTest {

    private MimeMessage signedMimeMessage;
    private SMimeMessageFactory sMimeMessageFactory;

    @Inject
    KeystoreManager keystoreManager;

    @BeforeMethod
    public void setUp() throws MimeTypeParseException {
        sMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());
        signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage("Arne Barne Busemann", new MimeType("text", "plain"));
    }

    @Test
    public void testCalculateMic() throws Exception {
        SignedMimeMessage signedMimeMessage = new SignedMimeMessage(this.signedMimeMessage);
        Mic mic1 = signedMimeMessage.calculateMic("sha1");
        assertNotNull(mic1);
        assertEquals(mic1.toString(), "Oqq8RQc3ff0SXMBXqh4fIwM8xGg=, sha1");
    }

    @Test
    public void testParseSignedMessage() throws Exception {
        SignedMimeMessage signedMimeMessage = new SignedMimeMessage(this.signedMimeMessage);
        try {
            signedMimeMessage.parseSignedMessage();
        } catch (Exception e){
            assertTrue(false, e.getMessage());
        }
        assertTrue(true);
    }

    @Test
    public void parseMessageWithSbdh() throws Exception {
        InputStream is = SignedMimeMessageTest.class.getClassLoader().getResourceAsStream("as2-peppol-bis-invoice-sbdh.xml");
        assertNotNull(is, "as2-peppol-bis-invoice-sbdh.xml not found in class path");
        MimeMessage signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage(is, new MimeType("application/xml"));
        SignedMimeMessage inspector = new SignedMimeMessage(signedMimeMessage);

        MessageDigestResult messageDigestResult = inspector.calcPayloadDigest("SHA-256");
        System.out.println(messageDigestResult.getAlgorithmName() + " Digest in Base64: " + messageDigestResult.getDigestAsString());
    }

}
