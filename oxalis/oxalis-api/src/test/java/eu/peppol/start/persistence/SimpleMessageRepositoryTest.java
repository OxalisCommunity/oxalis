package eu.peppol.start.persistence;

import eu.peppol.start.identifier.ChannelId;
import eu.peppol.start.identifier.MessageId;
import eu.peppol.start.identifier.ParticipantId;
import eu.peppol.start.identifier.PeppolMessageHeader;
import org.testng.annotations.BeforeTest;
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

    private PeppolMessageHeader peppolHeader;

    @BeforeTest
    public void createPeppolHeader() {
        peppolHeader = new PeppolMessageHeader();
        peppolHeader.setRecipientId(new ParticipantId("9908:976098897"));
        peppolHeader.setSenderId(new ParticipantId("9908:123456789"));
    }

    @Test
    public void computeDirectoryNameForMessage() throws IOException {
        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository();

        // Adds the channel id
        peppolHeader.setChannelId(new ChannelId("CH1"));

        String tmpdir = "/tmpx";

        File dirName = simpleMessageRepository.computeDirectoryNameForInboundMessage(tmpdir, peppolHeader);
        assertEquals(dirName.getCanonicalPath(), tmpdir + "/9908_976098897/CH1/9908_123456789", "Invalid directory name computed");
    }

    @Test
    public void computeDirectoryNameForMessageWithNoChannel() throws IOException {
        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository();

        PeppolMessageHeader h = new PeppolMessageHeader();
        h.setRecipientId(new ParticipantId("9908:976098897"));
        h.setSenderId(new ParticipantId("9908:123456789"));

        String tmpdir = "/tmpx";

        File dirName = simpleMessageRepository.computeDirectoryNameForInboundMessage(tmpdir, h);
        assertEquals(dirName.getCanonicalPath(), tmpdir + "/9908_976098897/9908_123456789", "Invalid directory name computed");
    }

    @Test
    public void computeDirectoryNameForOutboundMessages() throws IOException {
        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository();
        String tmpdir = "/tmpx";

        peppolHeader.setChannelId(new ChannelId(null));
        File dirName = simpleMessageRepository.computeDirectoryNameForOutboundMessages(tmpdir, peppolHeader);
        assertEquals(dirName.getCanonicalPath(), tmpdir + "/9908_123456789/9908_976098897", "Invalid directory name computed");
    }

    @Test
    public void testPrepareMessageStore() {
        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository();

        File root = File.listRoots()[0];
        File tmp = new File(root, "/tmp/X");
        try {
            tmp.mkdirs();
            System.err.println(tmp.toString());
            System.err.flush();
            PeppolMessageHeader peppolMessageHeader = new PeppolMessageHeader();
            peppolMessageHeader.setMessageId(new MessageId("uuid:c5aa916d-9a1e-4ae8-ba25-0709ec913acb"));
            peppolMessageHeader.setRecipientId(new ParticipantId("9908:976098897"));
            peppolMessageHeader.setSenderId(new ParticipantId("9908:123456789"));

            simpleMessageRepository.prepareMessageDirectory(tmp.toString(),peppolMessageHeader);
        } finally {
            tmp.delete();
        }
    }
}
