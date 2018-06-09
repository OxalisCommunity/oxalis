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

package no.difi.oxalis.as2.inbound;

import com.google.inject.Inject;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.as2.code.As2Header;
import no.difi.oxalis.as2.util.MimeMessageHelper;
import no.difi.oxalis.as2.util.SMimeDigestMethod;
import no.difi.oxalis.as2.util.SMimeMessageFactory;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import no.difi.oxalis.commons.persist.NoopPersister;
import no.difi.oxalis.commons.security.CertificateUtils;
import no.difi.oxalis.commons.tag.NoopTagGenerator;
import no.difi.oxalis.commons.transmission.DefaultTransmissionVerifier;
import no.difi.vefa.peppol.security.util.EmptyCertificateValidator;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

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
import java.util.Date;

import static org.testng.Assert.assertNotNull;

/**
 * Verifies that the As2InboundHandler works as expected.
 *
 * @author steinar
 * Date: 08.12.2015
 * Time: 15.21
 */
@Guice(modules = GuiceModuleLoader.class)
public class As2InboundHandlerTest {

    private InternetHeaders headers;

    private TimestampProvider mockTimestampProvider;

    @Inject
    private X509Certificate certificate;

    @Inject
    private PrivateKey privateKey;

    @Inject
    private SMimeMessageFactory sMimeMessageFactory;

    @BeforeClass
    public void setUp() throws Exception {
        mockTimestampProvider = Mockito.mock(TimestampProvider.class);
        Mockito.doReturn(new Timestamp(new Date(), null)).when(mockTimestampProvider)
                .generate(Mockito.any(), Mockito.any(Direction.class));
        Mockito.doReturn(new Timestamp(new Date(), null)).when(mockTimestampProvider)
                .generate(Mockito.any(), Mockito.any(Direction.class), Mockito.any());

        String ourCommonName = CertificateUtils.extractCommonName(certificate);

        headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS, "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO, ourCommonName);
        headers.addHeader(As2Header.AS2_FROM, ourCommonName);
        headers.addHeader(As2Header.MESSAGE_ID, "42");
        headers.addHeader(As2Header.AS2_VERSION, As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT, "An AS2 message");
        headers.addHeader(As2Header.DATE, "Mon Oct 21 22:01:48 CEST 2013");

    }

    @Test(enabled = false)
    public void testReceive() throws Exception {
        InputStream inputStream = loadSampleMimeMessage();

        As2InboundHandler as2InboundHandler = new As2InboundHandler(Mockito.mock(StatisticsService.class),
                mockTimestampProvider, EmptyCertificateValidator.INSTANCE, new NoopPersister(),
                new DefaultTransmissionVerifier(), sMimeMessageFactory, new NoopTagGenerator());

        MimeMessage mimeMessage = MimeMessageHelper.parse(inputStream, headers);
        as2InboundHandler.receive(headers, mimeMessage);
    }


    /**
     * Creates a fake S/MIME message, to mimic the data being posted in an http POST request.
     *
     * @return
     */
    InputStream loadSampleMimeMessage() {

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("sbdh-asic.xml");
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

