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

package eu.peppol.as2.inbound;

import com.google.inject.Inject;
import eu.peppol.as2.As2TestModule;
import eu.peppol.as2.util.As2Header;
import eu.peppol.as2.util.MdnMimeMessageFactory;
import eu.peppol.as2.util.MimeMessageHelper;
import eu.peppol.as2.util.SMimeMessageFactory;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.security.KeystoreManager;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import no.difi.oxalis.commons.security.CertificateUtils;
import no.difi.vefa.peppol.security.util.EmptyCertificateValidator;
import org.easymock.EasyMock;
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
import java.util.Date;

import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertNotNull;

/**
 * Verifies that the As2InboundHandler works as expected.
 *
 * @author steinar
 *         Date: 08.12.2015
 *         Time: 15.21
 */

@Guice(modules = {As2TestModule.class, As2InboundModule.class})
public class As2InboundHandlerTest {

    private InternetHeaders headers;
    private String ourCommonName;


    @Inject
    KeystoreManager keystoreManager;

    @Inject
    MdnMimeMessageFactory mdnMimeMessageFactory;

    TimestampProvider mockTimestampProvider;

    @BeforeClass
    public void setUp() throws Exception {
        mockTimestampProvider = Mockito.mock(TimestampProvider.class);
        Mockito.doReturn(new Timestamp(new Date(), null)).when(mockTimestampProvider).generate(Mockito.any());
        Mockito.doReturn(new Timestamp(new Date(), null)).when(mockTimestampProvider).generate(Mockito.any(), Mockito.any());

        ourCommonName = CertificateUtils.extractCommonName(keystoreManager.getOurCertificate());

        headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS, "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO, ourCommonName);
        headers.addHeader(As2Header.AS2_FROM, ourCommonName);
        headers.addHeader(As2Header.MESSAGE_ID, "42");
        headers.addHeader(As2Header.AS2_VERSION, As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT, "An AS2 message");
        headers.addHeader(As2Header.DATE, "Mon Oct 21 22:01:48 CEST 2013");

    }

    @Test
    public void testReceive() throws Exception {


        InputStream inputStream = loadSampleMimeMessage();

        MessageRepository mr = EasyMock.niceMock(MessageRepository.class);
        replay(mr);

        RawStatisticsRepository rawStatisticsRepository = new RawStatisticsRepository() {
            @Override
            public Integer persist(RawStatistics rawStatistics) {
                return 42;
            }

            @Override
            public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {

            }
        };

        As2InboundHandler as2InboundHandler = new As2InboundHandler(mdnMimeMessageFactory, mr, rawStatisticsRepository,
                mockTimestampProvider, new AccessPointIdentifier(ourCommonName), EmptyCertificateValidator.INSTANCE,
                (mi, h, in) -> null, m -> null, (mi, h) -> {
        });

        ResponseData responseData = as2InboundHandler.receive(headers, inputStream);
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

            SMimeMessageFactory sMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());
            MimeMessage signedMimeMessage = sMimeMessageFactory.createSignedMimeMessage(mimeBodyPart);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            signedMimeMessage.writeTo(os);

            return new ByteArrayInputStream(os.toByteArray());
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("Unable to write S/MIME message to byte array outputstream " + e.getMessage(), e);
        }
    }
}