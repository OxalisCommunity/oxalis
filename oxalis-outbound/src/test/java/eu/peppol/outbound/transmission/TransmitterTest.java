package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.security.CommonName;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.statistics.*;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import static org.testng.Assert.*;

/**
 * @author steinar
 * @author thore
 */
@Guice(modules = {TransmissionTestModule.class,TestResourceModule.class})
public class TransmitterTest {

    TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject
    OverridableTransmissionRequestBuilderCreator overridableTransmissionRequestBuilderCreator;

    @Inject
    @Named("sample-xml-with-sbdh")
    InputStream inputStream;

    @BeforeMethod
    public void setUp() {
        transmissionRequestBuilder = overridableTransmissionRequestBuilderCreator.createTansmissionRequestBuilderAllowingOverrides();
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
        Transmitter transmitter = new Transmitter(mockMessageSenderFactory, mockRepo, new CommonName("AP_TEST"));

        EasyMock.replay(mockRepo);
        transmitter.persistStatistics(transmissionRequest, transmissionResponse);

        assertNotNull(transmitter);

    }

}
