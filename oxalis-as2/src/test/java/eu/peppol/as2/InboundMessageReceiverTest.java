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
import eu.peppol.security.KeystoreManager;
import eu.peppol.security.OxalisCertificateValidator;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.StatisticsGranularity;
import eu.peppol.statistics.StatisticsTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

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
import java.util.Date;

import static org.testng.Assert.assertNotNull;

/** Verifies that the InboundMessageReceiver works as expected.
 *
 * @author steinar
 *         Date: 08.12.2015
 *         Time: 15.21
 */

@Guice(modules = {As2TestModule.class, As2Module.class})
public class InboundMessageReceiverTest {

    public static final Logger log = LoggerFactory.getLogger(InboundMessageReceiverTest.class);

    private InternetHeaders headers;
    private String ourCommonName ;


    @Inject
    KeystoreManager keystoreManager;

    @Inject OxalisCertificateValidator oxalisCertificateValidator;

    @Inject MdnMimeMessageFactory mdnMimeMessageFactory;

    @Inject
    As2TransmissionEvidenceFactory as2TransmissionEvidenceFactory;

    @BeforeClass
    public void setUp(){
        ourCommonName = keystoreManager.getOurCommonName().toString();

        headers = new InternetHeaders();
        headers.addHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName(), "Disposition-Notification-Options: signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        headers.addHeader(As2Header.AS2_TO.getHttpHeaderName(), PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX + ourCommonName.toString());
        headers.addHeader(As2Header.AS2_FROM.getHttpHeaderName(), PeppolAs2SystemIdentifier.AS2_SYSTEM_ID_PREFIX + ourCommonName.toString());
        headers.addHeader(As2Header.MESSAGE_ID.getHttpHeaderName(), "42");
        headers.addHeader(As2Header.AS2_VERSION.getHttpHeaderName(), As2Header.VERSION);
        headers.addHeader(As2Header.SUBJECT.getHttpHeaderName(), "An AS2 message");
        headers.addHeader(As2Header.DATE.getHttpHeaderName(), "Mon Oct 21 22:01:48 CEST 2013");

    }
    @Test
    public void testReceive() throws Exception {


        InputStream inputStream = loadSampleMimeMessage();

        MessageRepository messageRepository = new MessageRepository() {
            @Override
            public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream payload) throws OxalisMessagePersistenceException {

                log.debug("Persisting data!");
            }

            @Override
            public void saveTransportReceipt(TransmissionEvidence transmissionEvidence, PeppolMessageMetaData peppolMessageMetaData) {

            }

            @Override
            public void saveNativeTransportReceipt(byte[] bytes) {

            }
        };
        RawStatisticsRepository rawStatisticsRepository = new RawStatisticsRepository() {
            @Override
            public Integer persist(RawStatistics rawStatistics) {
                return 42;
            }

            @Override
            public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {

            }
        };

        InboundMessageReceiver inboundMessageReceiver = new InboundMessageReceiver(mdnMimeMessageFactory ,new SbdhFastParser(), new As2MessageInspector(keystoreManager) , messageRepository, rawStatisticsRepository, new AccessPointIdentifier(ourCommonName), oxalisCertificateValidator, as2TransmissionEvidenceFactory);


        ResponseData responseData = inboundMessageReceiver.receive(headers, inputStream );

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