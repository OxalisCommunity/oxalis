package eu.peppol.identifier;

import org.testng.annotations.Test;

import java.util.UUID;

/**
 * @author steinar
 *         Date: 08.11.13
 *         Time: 10:04
 */
public class MessageIdTest {
    @Test
    public void testToString() throws Exception {
        String messageId = "uuid:" + UUID.randomUUID();
        MessageId messageId1 = new MessageId(messageId);

        UUID uuid = messageId1.toUUID();
    }

    @Test
    public void uuidFromString() throws Exception {
        MessageId messageId = new MessageId("1070e7f0-3bae-11e3-aa6e-0800200c9a66");

    }
}
