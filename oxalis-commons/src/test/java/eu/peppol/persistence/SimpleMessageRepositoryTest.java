package eu.peppol.persistence;

import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 21:10
 */
public class SimpleMessageRepositoryTest {

    @Test
    public void computeDirectoryNameForMessage() throws IOException {
        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository(GlobalConfiguration.getInstance());

        ParticipantId recipientId = new ParticipantId("9908:976098897");
        ParticipantId senderId = new ParticipantId("9908:123456789");

        String tmpdir = "/tmpx";

        File dirName = simpleMessageRepository.computeDirectoryNameForInboundMessage(tmpdir, recipientId, senderId);
        
        assertEquals(dirName, new File(tmpdir + "/9908_976098897/9908_123456789"), "Invalid directory name computed");
    }

    @Test
    public void computeDirectoryNameForMessageWithNoChannel() throws IOException {

        ParticipantId recipientId = new ParticipantId("9908:976098897");
        ParticipantId senderId = new ParticipantId("9908:123456789");

        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository(GlobalConfiguration.getInstance());

        String tmpdir = "/tmpx";

        File dirName = simpleMessageRepository.computeDirectoryNameForInboundMessage(tmpdir, recipientId, senderId);
        assertEquals(dirName, new File(tmpdir + "/9908_976098897/9908_123456789"), "Invalid directory name computed");
    }

    @Test
    public void testPrepareMessageStore() {
        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository(GlobalConfiguration.getInstance());

        File tmpDir = new File(System.getProperty("java.io.tmpdir"));

        File tmp = new File(tmpDir, "/X");
        try {
            tmp.mkdirs();
            MessageId messageId = new MessageId("uuid:c5aa916d-9a1e-4ae8-ba25-0709ec913acb");
            ParticipantId recipientId = new ParticipantId("9908:976098897");
            ParticipantId senderId = new ParticipantId("9908:123456789");

            simpleMessageRepository.prepareMessageDirectory(tmp.toString(),recipientId, senderId);
        } finally {
            tmp.delete();
        }
    }
}
