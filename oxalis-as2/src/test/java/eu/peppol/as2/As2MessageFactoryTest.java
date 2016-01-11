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

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
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


    /**
     * Creates an As2Message based upon the sample http POST data in test/resources
     */
    @Test
    public void createAs2Message() throws MdnRequestException, InvalidAs2MessageException, IOException, MessagingException {

        // Loads the sample data to mimic an http post request
        InternetHeaders internetHeaders = loadSampleHeaders();
        InputStream samplePostRequestEntityStream = createInputStream();

        // Creates the AS2Message
        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessageAssistedByHeaders(samplePostRequestEntityStream, internetHeaders);
        SignedMimeMessage signedMimeMessage = new SignedMimeMessage(mimeMessage);

        As2Message as2Message = As2MessageFactory.createAs2MessageFrom(internetHeaders, signedMimeMessage);

        assertNotNull(as2Message);

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

    private InternetHeaders loadSampleHeaders() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sample-as2-post-headers.txt")) {
            InternetHeaders internetHeaders = new InternetHeaders(is);
            return internetHeaders;
        } catch (IOException | MessagingException e) {
            throw new IllegalStateException("Unable to load data from sample-as2-post-headers.txt", e);
        }
    }

    public InputStream createInputStream() {
        InputStream resourceAsStream = As2MessageFactoryTest.class.getClassLoader().getResourceAsStream("sample-as2-post-request.txt");
        assertNotNull(resourceAsStream);
        return resourceAsStream;
    }

}
