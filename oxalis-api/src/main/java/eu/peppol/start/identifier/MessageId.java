package eu.peppol.start.identifier;

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
    
    @Override
    public String toString() {
        return value;
    }
}
