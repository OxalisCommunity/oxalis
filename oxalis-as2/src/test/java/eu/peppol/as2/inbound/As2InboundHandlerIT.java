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
import eu.peppol.as2.model.As2Disposition;
import eu.peppol.as2.model.MdnData;
import eu.peppol.as2.util.As2Header;
import eu.peppol.as2.util.MdnMimeMessageFactory;
import eu.peppol.as2.util.SMimeMessageFactory;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.security.KeystoreManager;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisKeystoreModule;
import eu.peppol.util.OxalisProductionConfigurationModule;
import no.difi.oxalis.commons.security.CertificateUtils;
import no.difi.oxalis.api.timestamp.Timestamp;
import no.difi.oxalis.api.timestamp.TimestampProvider;
import org.easymock.EasyMock;
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
import java.io.*;
import java.util.Date;

import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Simulates reception of a an AS2 Message, which is validated etc. and finally produces an MDN.
 *
 * @author steinar
 * @author thore
 */
@Test(groups = {"integration"})
@Guice(modules = {OxalisKeystoreModule.class, OxalisProductionConfigurationModule.class})
public class As2InboundHandlerIT {

    @Inject
    GlobalConfiguration globalConfiguration;
    @Inject
    KeystoreManager keystoreManager;

    private ByteArrayInputStream inputStream;
    private InternetHeaders headers;
    private RawStatisticsRepository rawStatisticsRepository = createFailingStatisticsRepository();

    private AccessPointIdentifier ourAccessPointIdentifier;
    private MdnMimeMessageFactory mdnMimeMessageFactory;
    private MessageRepository fakeMessageRepository;

    private TimestampProvider mockTimestampProvider;

    @BeforeClass
    public void beforeClass() throws Exception {
        mockTimestampProvider = Mockito.mock(TimestampProvider.class);
        Mockito.doReturn(new Timestamp(new Date(), null)).when(mockTimestampProvider).generate(Mockito.any());
        Mockito.doReturn(new Timestamp(new Date(), null)).when(mockTimestampProvider).generate(Mockito.any(), Mockito.any());
    }

    @BeforeMethod
    public void createHeaders() {
        File inboundMessageStore = new File(globalConfiguration.getInboundMessageStore());
        String ourCommonName = CertificateUtils.extractCommonName(keystoreManager.getOurCertificate());
        ourAccessPointIdentifier = new AccessPointIdentifier(ourCommonName);

        headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS, "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO, ourCommonName);
        headers.addHeader(As2Header.AS2_FROM, ourCommonName);
        headers.addHeader(As2Header.MESSAGE_ID, "42");
        headers.addHeader(As2Header.AS2_VERSION, As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT, "An AS2 message");
        headers.addHeader(As2Header.DATE, "Mon Oct 21 22:01:48 CEST 2013");
    }

    @BeforeMethod
    public void createInputStream() throws MimeTypeParseException, IOException, MessagingException {
        SMimeMessageFactory SMimeMessageFactory = new SMimeMessageFactory(keystoreManager.getOurPrivateKey(), keystoreManager.getOurCertificate());

        // Fetch input stream for data
        InputStream resourceAsStream = SMimeMessageFactory.class.getClassLoader().getResourceAsStream("as2-peppol-bis-invoice-sbdh.xml");
        assertNotNull(resourceAsStream);

        // Creates the signed message
        MimeMessage signedMimeMessage = SMimeMessageFactory.createSignedMimeMessage(resourceAsStream, new MimeType("application", "xml"));
        assertNotNull(signedMimeMessage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        signedMimeMessage.writeTo(baos);

        inputStream = new ByteArrayInputStream(baos.toByteArray());

        signedMimeMessage.writeTo(System.out);

        mdnMimeMessageFactory = new MdnMimeMessageFactory(keystoreManager.getOurCertificate(), keystoreManager.getOurPrivateKey());

        fakeMessageRepository = EasyMock.niceMock(MessageRepository.class);
        replay(fakeMessageRepository);
    }

    private RawStatisticsRepository createFailingStatisticsRepository() {
        return new RawStatisticsRepository() {
            @Override
            public Integer persist(RawStatistics rawStatistics) {
                throw new IllegalStateException("Persistence of statistics failed, but this should not break the message reception");
            }

            @Override
            public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {
            }
        };
    }

    public void loadAndReceiveTestMessageOK() throws Exception {

        As2InboundHandler as2InboundHandler = new As2InboundHandler(mdnMimeMessageFactory, fakeMessageRepository, rawStatisticsRepository, ourAccessPointIdentifier, mockTimestampProvider);

        ResponseData responseData = as2InboundHandler.receive(headers, inputStream);

        assertEquals(responseData.getMdnData().getAs2Disposition().getDispositionType(), As2Disposition.DispositionType.PROCESSED);
        assertNotNull(responseData.getMdnData().getMic());
    }

    /**
     * Specifies an invalid MIC algorithm (MD5), which should cause reception to fail.
     *
     * @throws Exception
     */
    public void receiveMessageWithInvalidDispositionRequest() throws Exception {

        headers.setHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS, "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,md5");

        As2InboundHandler as2InboundHandler = new As2InboundHandler(mdnMimeMessageFactory, fakeMessageRepository, rawStatisticsRepository, ourAccessPointIdentifier, mockTimestampProvider);

        ResponseData responseData = as2InboundHandler.receive(headers, inputStream);

        assertEquals(responseData.getMdnData().getAs2Disposition().getDispositionType(), As2Disposition.DispositionType.FAILED);
        assertEquals(responseData.getMdnData().getSubject(), MdnData.SUBJECT);
    }
}
