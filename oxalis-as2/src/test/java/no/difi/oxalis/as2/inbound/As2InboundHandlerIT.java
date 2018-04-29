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
import no.difi.oxalis.api.model.AccessPointIdentifier;
import no.difi.oxalis.api.model.Direction;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.as2.code.As2Header;
import no.difi.oxalis.as2.util.MimeMessageHelper;
import no.difi.oxalis.as2.util.SMimeDigestMethod;
import no.difi.oxalis.as2.util.SMimeMessageFactory;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import no.difi.oxalis.commons.persist.NoopPersister;
import no.difi.oxalis.commons.statistics.NoopStatisticsService;
import no.difi.oxalis.commons.transmission.DefaultTransmissionVerifier;
import no.difi.vefa.peppol.security.util.EmptyCertificateValidator;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
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
 * Simulates reception of a an AS2 Message, which is validated etc. and finally produces an MDN.
 *
 * @author steinar
 * @author thore
 */
@Guice(modules = {GuiceModuleLoader.class})
public class As2InboundHandlerIT {

    private ByteArrayInputStream inputStream;

    private InternetHeaders headers;

    private TimestampProvider mockTimestampProvider;

    @Inject
    private PrivateKey privateKey;

    @Inject
    private X509Certificate certificate;

    @Inject
    private AccessPointIdentifier accessPointIdentifier;

    @Inject
    private SMimeMessageFactory sMimeMessageFactory;

    @BeforeClass
    public void beforeClass() throws Exception {
        mockTimestampProvider = Mockito.mock(TimestampProvider.class);
        Mockito.doReturn(new Timestamp(new Date(), null))
                .when(mockTimestampProvider).generate(Mockito.any(), Mockito.any(Direction.class));
        Mockito.doReturn(new Timestamp(new Date(), null))
                .when(mockTimestampProvider).generate(Mockito.any(), Mockito.any(Direction.class), Mockito.any());
    }

    @BeforeMethod
    public void createHeaders() {
        headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS,
                "signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO, accessPointIdentifier.toString());
        headers.addHeader(As2Header.AS2_FROM, accessPointIdentifier.toString());
        headers.addHeader(As2Header.MESSAGE_ID, "42");
        headers.addHeader(As2Header.AS2_VERSION, As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT, "An AS2 message");
        headers.addHeader(As2Header.DATE, "Mon Oct 21 22:01:48 CEST 2013");
    }

    @BeforeMethod
    public void createInputStream() throws MimeTypeParseException, IOException, MessagingException,
            OxalisTransmissionException {
        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(privateKey, certificate);

        // Fetch input stream for data
        InputStream resourceAsStream = getClass().getResourceAsStream("/as2-peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream);

        // Creates the signed message
        MimeMessage signedMimeMessage = SMimeMessageFactory
                .createSignedMimeMessage(resourceAsStream, new MimeType("application", "xml"), SMimeDigestMethod.sha1);
        assertNotNull(signedMimeMessage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        signedMimeMessage.writeTo(baos);

        inputStream = new ByteArrayInputStream(baos.toByteArray());

        signedMimeMessage.writeTo(System.out);
    }

    public void loadAndReceiveTestMessageOK() throws Exception {

        As2InboundHandler as2InboundHandler = new As2InboundHandler(
                new NoopStatisticsService(), mockTimestampProvider, EmptyCertificateValidator.INSTANCE,
                new NoopPersister(), new DefaultTransmissionVerifier(), sMimeMessageFactory);

        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessageAssistedByHeaders(inputStream, headers);
        MimeMessage mdn = as2InboundHandler.receive(headers, mimeMessage);

        // ResponseData responseData = as2InboundHandler.receive(headers, inputStream);

        // assertEquals(responseData.getMdnData().getAs2Disposition().getDispositionType(),
        // As2Disposition.DispositionType.PROCESSED);
        // assertNotNull(responseData.getMdnData().getMic());
    }

    /**
     * Specifies an invalid MIC algorithm (MD5), which should cause reception to fail.
     */
    @Test(enabled = false)
    public void receiveMessageWithInvalidDispositionRequest() throws Exception {

        headers.setHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS, "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,md5");

        As2InboundHandler as2InboundHandler = new As2InboundHandler(new NoopStatisticsService(),
                mockTimestampProvider, EmptyCertificateValidator.INSTANCE, new NoopPersister(),
                new DefaultTransmissionVerifier(), sMimeMessageFactory);

        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessageAssistedByHeaders(inputStream, headers);
        MimeMessage mdn = as2InboundHandler.receive(headers, mimeMessage);

        // assertEquals(responseData.getMdnData().getAs2Disposition().getDispositionType(),
        // As2Disposition.DispositionType.FAILED);
        // assertEquals(responseData.getMdnData().getSubject(), MdnData.SUBJECT);
    }
}
