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
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.as2.code.As2Header;
import network.oxalis.commons.security.CertificateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.testng.Assert.assertNotNull;


/**
 * @author steinar
 *         Date: 18.01.2016
 *         Time: 20.34
 */
public class TestDataGenerator {

    @Inject
    private X509Certificate certificate;

    @Inject
    private PrivateKey privateKey;

    public InternetHeaders createSampleInternetHeaders() {
        String participant = CertificateUtils.extractCommonName(certificate);

        InternetHeaders headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS, "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO, participant);
        headers.addHeader(As2Header.AS2_FROM, participant);
        headers.addHeader(As2Header.MESSAGE_ID, "42");
        headers.addHeader(As2Header.AS2_VERSION, As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT, "An AS2 message");
        headers.addHeader(As2Header.DATE, "Mon Oct 21 22:01:48 CEST 2013");
        return headers;
    }

    /**
     * Creates a fake S/MIME message, to mimic the data being posted in an http POST request.
     */
    public InputStream loadSbdhAsicXml() {

        InputStream resourceAsStream = TestDataGenerator.class.getClassLoader().getResourceAsStream("sbdh-asic.xml");
        assertNotNull(resourceAsStream);

        try {
            MimeBodyPart mimeBodyPart = MimeMessageHelper.createMimeBodyPart(resourceAsStream, "application/xml");

            SMimeMessageFactory sMimeMessageFactory = new SMimeMessageFactory(privateKey, certificate);
            MimeMessage signedMimeMessage = sMimeMessageFactory
                    .createSignedMimeMessage(mimeBodyPart, SMimeDigestMethod.sha1);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            signedMimeMessage.writeTo(os);

            return new ByteArrayInputStream(os.toByteArray());
        } catch (MessagingException | IOException | OxalisTransmissionException e) {
            throw new IllegalStateException("Unable to write S/MIME message to byte array outputstream " + e.getMessage(), e);
        }
    }
}
