package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.outbound.guice.SmpTestModule;
import eu.peppol.outbound.guice.TestResourceModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 04.11.13
 *         Time: 10:05
 */
@Guice(modules = {SmpTestModule.class, TestResourceModule.class})
public class TransmissionRequestBuilderTest {

    @Inject
    TransmissionRequestBuilder transmissionRequestBuilder;

    @Inject @Named("sampleXml")
    InputStream inputStream;

    @Test
    public void createTransmissionRequestBuilderWithOnlyTheMessageDocument() throws Exception {

        assertNotNull(transmissionRequestBuilder);
        assertNotNull(inputStream);
        assertNotNull(transmissionRequestBuilder.sbdhParser);

        transmissionRequestBuilder.contentWithStandardBusinessHeader(inputStream);

        PeppolStandardBusinessHeader sbdh = transmissionRequestBuilder.getPeppolStandardBusinessHeader();
        assertNotNull(sbdh);

        assertEquals(sbdh.getRecipientId(), new ParticipantId("0007:4455454480"));

    }

}
