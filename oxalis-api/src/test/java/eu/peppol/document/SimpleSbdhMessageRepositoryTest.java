package eu.peppol.document;

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.identifier.ParticipantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 24.10.13
 *         Time: 14:11
 */
public class SimpleSbdhMessageRepositoryTest {

    public static final Logger log = LoggerFactory.getLogger(SimpleSbdhMessageRepositoryTest.class);

    public static final String senderAp = "AP_1000006";
    private PeppolMessageMetaData transmissionData;

    @BeforeMethod
    public PeppolMessageMetaData setUp() {

        transmissionData = new PeppolMessageMetaData();
        transmissionData.setMessageId(UUID.randomUUID().toString());
        transmissionData.setRecipientId(new ParticipantId("9908:976098897"));
        transmissionData.setSenderId(new ParticipantId("9908:81001709"));
        transmissionData.setSendingAccessPoint(senderAp);

        return transmissionData;
    }

    @Test
    public void testPrepareMessageDirectory() throws Exception {

        SimpleSbdhMessageRepository repository = new SimpleSbdhMessageRepository("/tmp");

        File file = repository.prepareMessageDirectory(transmissionData);
        String regexp = "9908_976098897/" + senderAp + "/9908_81001709";

        String name = file.getAbsolutePath();
        assertTrue(name.contains(regexp),name + " != " + regexp);
    }


    @Test
    public void testPersist() {
        SimpleSbdhMessageRepository repository = new SimpleSbdhMessageRepository("/tmp");

        InputStream resourceAsStream = SimpleSbdhMessageRepositoryTest.class.getClassLoader().getResourceAsStream("oxalis-statistics.properties");

        try {
            repository.persist(transmissionData, resourceAsStream);

            File messageFile = repository.getMessageFile(transmissionData);
            assertTrue(messageFile.isFile() && messageFile.canRead());

        } catch (SbdhMessageException e) {
            fail("Did not expect an eror; " + e.getMessage());
        }
    }
}
