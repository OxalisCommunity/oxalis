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
}
