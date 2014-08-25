package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.outbound.guice.TestResourceModule;
import eu.peppol.security.CommonName;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.statistics.*;
import org.easymock.EasyMock;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 23.11.13
 *         Time: 20:55
 */
@Guice(modules = {TransmissionTestModule.class,TestResourceModule.class})
public class TransmitterTest {

    @Inject
    TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject
    @Named("sample-xml-with-sbdh")
    InputStream inputStream;

    @Test
    public void testPersistStatistics() throws Exception {

        transmissionRequestBuilder.payLoad(inputStream);
        final TransmissionRequest transmissionRequest = transmissionRequestBuilder.build();

        TransmissionResponse transmissionResponse = new TransmissionResponse() {
            @Override
            public TransmissionId getTransmissionId() {
                return new TransmissionId();
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
