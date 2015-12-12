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
import eu.peppol.util.RuntimeConfigurationModule;
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
            public KeyStore loadOurKeystore(String password) {
                return null;
            }

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
            public PrivateKey getOurPrivateKey(KeyStore keyStore, String password) {
                return null;
            }

            @Override
            public KeyStore loadPeppolTruststore() {
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
