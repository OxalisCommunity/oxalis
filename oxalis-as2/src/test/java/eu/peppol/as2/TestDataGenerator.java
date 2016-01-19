/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertNotNull;


/**
 * @author steinar
 *         Date: 18.01.2016
 *         Time: 20.34
 */
public class TestDataGenerator {

    @Inject
    KeystoreManager keystoreManager;


    public InternetHeaders createSampleInternetHeaders() {
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO.getHttpHeaderName(), PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX + keystoreManager.getOurCommonName().toString());
        headers.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX + keystoreManager.getOurCommonName().toString());
        headers.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), "42");
        headers.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "An AS2 message");
        headers.addHeader(As2Header.DATE.getHttpHeaderName(), "Mon Oct 21 22:01:48 CEST 2013");
        return headers;
    }

    /**
     * Creates a fake S/MIME message, to mimic the data being posted in an http POST request.
     *
     * @return
     */
    public  InputStream loadSbdhAsicXml() {

        InputStream resourceAsStream = TestDataGenerator.class.getClassLoader().getResourceAsStream("sbdh-asic.xml");
        assertNotNull(resourceAsStream);

        try {
            MimeBodyPart mimeBodyPart = MimeMessageHelper.createMimeBodyPart(resourceAsStream, new MimeType("application/xml"));

            SMimeMessageFactory sMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());
            MimeMessage signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage(mimeBodyPart);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            signedMimeMessage.writeTo(os);

            return new ByteArrayInputStream(os.toByteArray());

        } catch (MimeTypeParseException e) {
            throw new IllegalStateException("Invalid mime type " + e.getMessage(), e);
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("Unable to write S/MIME message to byte array outputstream " + e.getMessage(), e);
        }
    }
}
