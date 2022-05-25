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

import com.google.inject.Inject;
import network.oxalis.commons.guice.GuiceModuleLoader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 * Date: 08.10.13
 * Time: 11:34
 */
@Guice(modules = GuiceModuleLoader.class)
public class SMimeMessageFactoryTest {

    private network.oxalis.as2.util.SMimeMessageFactory SMimeMessageFactory;

    private InputStream resourceAsStream;

    @Inject
    private PrivateKey privateKey;

    @Inject
    private X509Certificate certificate;

    @BeforeMethod
    public void createMimeMessageFactory() {
        SMimeMessageFactory = new SMimeMessageFactory(privateKey, certificate);

        // Fetches input stream for data
        resourceAsStream = SMimeMessageFactory.class.getClassLoader().getResourceAsStream("example.xml");
        assertNotNull(resourceAsStream);

    }

    @Test
    public void inspectSignedMessage() throws Exception {


        // Creates the signed message
        MimeMessage signedMimeMessage = SMimeMessageFactory
                .createSignedMimeMessage(resourceAsStream, new MimeType("application", "xml"), SMimeDigestMethod.sha1);
        assertNotNull(signedMimeMessage);

        assertTrue(signedMimeMessage.getContent() instanceof MimeMultipart, "Not a MultiPart");

        // Converts the contents into a Mime Multi Part, which should consist of two body parts
        MimeMultipart mimeMultipart = (MimeMultipart) signedMimeMessage.getContent();

        // First part contains the payload
        BodyPart bodyPart = mimeMultipart.getBodyPart(0);
        // For which the contents is an input stream giving access to the actual data
        Object content = bodyPart.getContent();
        assertTrue(content instanceof InputStream);

        StringWriter sw = new StringWriter();
        int c;
        InputStream inputStream = bodyPart.getInputStream();
        while ((c = inputStream.read()) >= 0) {
            sw.write(c);
        }

        assertTrue(sw.toString().contains("<?xml version"));
    }
}

