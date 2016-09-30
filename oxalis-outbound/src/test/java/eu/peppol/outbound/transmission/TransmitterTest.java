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

package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.statistics.*;
import eu.peppol.util.GlobalConfiguration;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.testng.Assert.*;

/**
 * @author steinar
 * @author thore
 */
@Guice(modules = {TransmissionTestModule.class,TestResourceModule.class})
public class TransmitterTest {

    @Inject
    TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject
    GlobalConfiguration globalConfiguration;

    @Inject
    @Named("sample-xml-with-sbdh")
    InputStream inputStream;

    @BeforeMethod
    public void setUp() {

        globalConfiguration.setTransmissionBuilderOverride(true);
    }

    @Test
    public void testPersistStatistics() throws Exception {

        transmissionRequestBuilder.payLoad(inputStream);

        final TransmissionRequest transmissionRequest = transmissionRequestBuilder.build();

        TransmissionResponse transmissionResponse = new TransmissionResponse() {
            @Override
            public TransmissionId getTransmissionId() {
                return new TransmissionId();
            }

            @Override
            public PeppolStandardBusinessHeader getStandardBusinessHeader() {
                return null;
            }

            @Override
            public URL getURL() {
                return null;
            }

            @Override
            public BusDoxProtocol getProtocol() {
                return null;
            }

            @Override
            public CommonName getCommonName() {
                return null;
            }

            @Override
            public byte[] getEvidenceBytes() {
                return null;
            }
        };

        MessageSenderFactory mockMessageSenderFactory = EasyMock.createMock(MessageSenderFactory.class);
        RawStatisticsRepository mockRepo = EasyMock.createMock(RawStatisticsRepository.class);

        EasyMock.expect(mockRepo.persist(EasyMock.isA(RawStatistics.class))).andDelegateTo(new RawStatisticsRepository() {
            @Override
            public Integer persist(RawStatistics rawStatistics) {
                assertNotNull(rawStatistics.getReceiver());
                assertNotNull(rawStatistics.getSender());
                assertNotNull(rawStatistics.getAccessPointIdentifier());
                assertEquals(rawStatistics.getAccessPointIdentifier(), new AccessPointIdentifier("AP_TEST"));
                assertNotNull(rawStatistics.getDate());
                assertNotEquals(rawStatistics.getDate(), transmissionRequest.getPeppolStandardBusinessHeader().getCreationDateAndTime());

                assertEquals(rawStatistics.getDirection(), Direction.OUT);
                assertEquals(rawStatistics.getPeppolDocumentTypeId(), transmissionRequest.getPeppolStandardBusinessHeader().getDocumentTypeIdentifier());

                assertNotNull(rawStatistics.getChannelId());
                assertNotNull(rawStatistics.getPeppolProcessTypeId());
                return 42;  // Fake primary key from the database.
            }

            @Override
            public void fetchAndTransformRawStatistics(StatisticsTransformer transformer, Date start, Date end, StatisticsGranularity granularity) {
            }
        });
        Transmitter transmitter = new Transmitter(mockMessageSenderFactory, mockRepo, new KeystoreManager() {

            @Override
            public KeyStore getPeppolTrustedKeyStore() {
                return null;
            }

            @Override
            public KeyStore getOurKeystore() {
                return null;
            }

            @Override
            public X509Certificate getOurCertificate() {
                return null;
            }

            @Override
            public CommonName getOurCommonName() {
                return new CommonName("AP_TEST");
            }

            @Override
            public PrivateKey getOurPrivateKey() {
                return null;
            }


            @Override
            public boolean isOurCertificate(X509Certificate candidate) {
                return false;
            }
        });

        EasyMock.replay(mockRepo);
        transmitter.persistStatistics(transmissionRequest, transmissionResponse);

        assertNotNull(transmitter);

    }

}
