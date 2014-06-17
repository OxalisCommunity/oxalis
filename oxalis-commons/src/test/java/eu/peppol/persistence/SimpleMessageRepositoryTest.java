package eu.peppol.persistence;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.identifier.*;
import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

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

    @Test
    public void verifyFullHeadersAsJSON() {

        PeppolMessageMetaData metadata = new PeppolMessageMetaData();
        metadata.setMessageId(new MessageId(UUID.randomUUID().toString()));
        metadata.setRecipientId(new ParticipantId("9908:976098897"));
        metadata.setSenderId(new ParticipantId("9908:976098897"));
        metadata.setDocumentTypeIdentifier(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        metadata.setProfileTypeIdentifier(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId());
        metadata.setSendingAccessPoint(new AccessPointIdentifier("XX_9876543210"));
        metadata.setReceivingAccessPoint(new AccessPointIdentifier("YY_0123456789"));
        metadata.setProtocol(BusDoxProtocol.AS2);
        metadata.setUserAgent("IDEA-Agent");
        metadata.setUserAgentVersion("v9");
        metadata.setSendersTimeStamp(new Date());
        metadata.setReceivedTimeStamp(new Date());
        metadata.setSendingAccessPointPrincipal(createPrincipal());
        metadata.setTransmissionId(new TransmissionId());

        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository(GlobalConfiguration.getInstance());
        String jsonString = simpleMessageRepository.getHeadersAsJSON(metadata);

        try {
            JsonParser parser = new JsonParser();
            parser.parse(jsonString);
        } catch (JsonSyntaxException ex) {
            fail("Illegal JSON produced : " + jsonString);
        }
    }

    @Test
    public void verifyEmptyHeadersAsJSON() {

        PeppolMessageMetaData metadata = new PeppolMessageMetaData();
        // no values set, most should be "null", validate that we still has valid JSON

        SimpleMessageRepository simpleMessageRepository = new SimpleMessageRepository(GlobalConfiguration.getInstance());
        String jsonString = simpleMessageRepository.getHeadersAsJSON(metadata);

        try {
            JsonParser parser = new JsonParser();
            parser.parse(jsonString);
        } catch (JsonSyntaxException ex) {
            fail("Illegal JSON produced : " + jsonString);
        }

    }

    private Principal createPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return "SOME_AP_PRINCIPAL";
            }
        };
    }


}
