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
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.as2.evidence.As2TransmissionEvidenceFactory;
import eu.peppol.as2.servlet.ResponseData;
import eu.peppol.document.SbdhFastParser;
import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.persistence.SimpleMessageRepository;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.security.OxalisCertificateValidator;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisCommonsModule;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Simulates reception of a an AS2 Message, which is validated etc. and finally produces an MDN.
 *
 * @author steinar
 * @author thore
 */
@Test(groups = {"integration"})
@Guice(modules = {OxalisCommonsModule.class})
public class InboundMessageReceiverIT {

    @Inject
    GlobalConfiguration globalConfiguration;
    @Inject
    KeystoreManager keystoreManager;

    @Inject
    OxalisCertificateValidator oxalisCertificateValidator;

    @Inject
    As2TransmissionEvidenceFactory as2TransmissionEvidenceFactory;

    private ByteArrayInputStream inputStream;
    private InternetHeaders headers;
    private MessageRepository messageRepository;
    private RawStatisticsRepository rawStatisticsRepository = createFailingStatisticsRepository();

    private AccessPointIdentifier ourAccessPointIdentifier;
    private MdnMimeMessageFactory mdnMimeMessageFactory;
    private MessageRepository fakeMessageRepository;

    @BeforeMethod
    public void createHeaders() {
        File inboundMessageStore = new File(globalConfiguration.getInboundMessageStore());
        messageRepository = new SimpleMessageRepository(inboundMessageStore);
        CommonName ourCommonName = keystoreManager.getOurCommonName();
        ourAccessPointIdentifier = AccessPointIdentifier.valueOf(keystoreManager.getOurCommonName());

        headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO.getHttpHeaderName(), PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX + ourCommonName.toString());
        headers.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX + ourCommonName.toString());
        headers.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), "42");
        headers.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "An AS2 message");
        headers.addHeader(As2Header.DATE.getHttpHeaderName(), "Mon Oct 21 22:01:48 CEST 2013");
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

        fakeMessageRepository = new MessageRepository() {
            @Override
            public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream payload) throws OxalisMessagePersistenceException {

            }

            @Override
            public void saveTransportReceipt(TransmissionEvidence transmissionEvidence, PeppolMessageMetaData peppolMessageMetaData) {

            }

            @Override
            public void saveNativeTransportReceipt(byte[] bytes) {

            }
        };


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

        InboundMessageReceiver inboundMessageReceiver = new InboundMessageReceiver(mdnMimeMessageFactory, new SbdhFastParser(), new As2MessageInspector(keystoreManager), fakeMessageRepository, rawStatisticsRepository, ourAccessPointIdentifier, oxalisCertificateValidator, as2TransmissionEvidenceFactory);

        ResponseData responseData = inboundMessageReceiver.receive(headers, inputStream);

        assertEquals(responseData.getMdnData().getAs2Disposition().getDispositionType(), As2Disposition.DispositionType.PROCESSED);
        assertNotNull(responseData.getMdnData().getMic());
    }

    /**
     * Specifies an invalid MIC algorithm (MD5), which should cause reception to fail.
     *
     * @throws Exception
     */
    public void receiveMessageWithInvalidDispositionRequest() throws Exception {

        headers.setHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,md5");

        InboundMessageReceiver inboundMessageReceiver = new InboundMessageReceiver(mdnMimeMessageFactory, new SbdhFastParser(), new As2MessageInspector(keystoreManager), fakeMessageRepository, rawStatisticsRepository, ourAccessPointIdentifier, oxalisCertificateValidator, as2TransmissionEvidenceFactory);

        ResponseData responseData = inboundMessageReceiver.receive(headers, inputStream);

        assertEquals(responseData.getMdnData().getAs2Disposition().getDispositionType(), As2Disposition.DispositionType.FAILED);
        assertEquals(responseData.getMdnData().getSubject(), MdnData.SUBJECT);
    }
}
