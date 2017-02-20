/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.as2.inbound;

import com.google.inject.Inject;
import eu.peppol.as2.code.As2Header;
import eu.peppol.as2.model.As2Disposition;
import eu.peppol.as2.model.MdnData;
import eu.peppol.as2.util.MdnMimeMessageFactory;
import eu.peppol.as2.util.SMimeMessageFactory;
import eu.peppol.identifier.MessageId;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.model.AccessPointIdentifier;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import no.difi.vefa.peppol.common.model.Header;
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
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Simulates reception of a an AS2 Message, which is validated etc. and finally produces an MDN.
 *
 * @author steinar
 * @author thore
 */
@Test(groups = {"integration"})
@Guice(modules = {GuiceModuleLoader.class})
public class As2InboundHandlerIT {

    private ByteArrayInputStream inputStream;

    private InternetHeaders headers;

    private MdnMimeMessageFactory mdnMimeMessageFactory;

    private TimestampProvider mockTimestampProvider;

    @Inject
    private PrivateKey privateKey;

    @Inject
    private X509Certificate certificate;

    @Inject
    private AccessPointIdentifier accessPointIdentifier;

    @BeforeClass
    public void beforeClass() throws Exception {
        mockTimestampProvider = Mockito.mock(TimestampProvider.class);
        Mockito.doReturn(new Timestamp(new Date(), null))
                .when(mockTimestampProvider).generate(Mockito.any());
        Mockito.doReturn(new Timestamp(new Date(), null))
                .when(mockTimestampProvider).generate(Mockito.any(), Mockito.any());
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
    public void createInputStream() throws MimeTypeParseException, IOException, MessagingException {
        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(privateKey, certificate);

        // Fetch input stream for data
        InputStream resourceAsStream = getClass().getResourceAsStream("/as2-peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream);

        // Creates the signed message
        MimeMessage signedMimeMessage = SMimeMessageFactory
                .createSignedMimeMessage(resourceAsStream, new MimeType("application", "xml"));
        assertNotNull(signedMimeMessage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        signedMimeMessage.writeTo(baos);

        inputStream = new ByteArrayInputStream(baos.toByteArray());

        signedMimeMessage.writeTo(System.out);

        mdnMimeMessageFactory = new MdnMimeMessageFactory(certificate, privateKey);

    }

    public void loadAndReceiveTestMessageOK() throws Exception {

        As2InboundHandler as2InboundHandler = new As2InboundHandler(mdnMimeMessageFactory,
                Mockito.mock(StatisticsService.class), mockTimestampProvider, EmptyCertificateValidator.INSTANCE,
                new PersisterHandler() {
                    @Override
                    public Path persist(MessageId messageId, Header header, InputStream inputStream) throws IOException {
                        return null;
                    }

                    @Override
                    public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
                    }
                }, (h, d) -> {
        });

        ResponseData responseData = as2InboundHandler.receive(headers, inputStream);

        assertEquals(responseData.getMdnData().getAs2Disposition().getDispositionType(),
                As2Disposition.DispositionType.PROCESSED);
        assertNotNull(responseData.getMdnData().getMic());
    }

    /**
     * Specifies an invalid MIC algorithm (MD5), which should cause reception to fail.
     */
    @Test(enabled = false)
    public void receiveMessageWithInvalidDispositionRequest() throws Exception {

        headers.setHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS, "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,md5");

        As2InboundHandler as2InboundHandler = new As2InboundHandler(mdnMimeMessageFactory, Mockito.mock(StatisticsService.class),
                mockTimestampProvider, EmptyCertificateValidator.INSTANCE,
                new PersisterHandler() {
                    @Override
                    public Path persist(MessageId messageId, Header header, InputStream inputStream) throws IOException {
                        return null;
                    }

                    @Override
                    public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
                    }
                }, (h, d) -> {
        });

        ResponseData responseData = as2InboundHandler.receive(headers, inputStream);

        assertEquals(responseData.getMdnData().getAs2Disposition().getDispositionType(),
                As2Disposition.DispositionType.FAILED);
        assertEquals(responseData.getMdnData().getSubject(), MdnData.SUBJECT);
    }
}
