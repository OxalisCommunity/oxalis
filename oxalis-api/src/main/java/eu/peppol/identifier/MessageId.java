package eu.peppol.identifier;

import java.util.UUID;

/**
 * @author Steinar Overbeck Cook
 * 
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 18:44
 */
public class MessageId {
    String value;

    public MessageId(String messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("MessageId requires a non-null string");
        }
        value = messageId;
    }

    public String stringValue(){
        return value;
    }

    public static MessageId getUniqueId() {
        return new MessageId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
