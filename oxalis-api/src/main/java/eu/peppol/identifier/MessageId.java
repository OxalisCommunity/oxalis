package eu.peppol.identifier;

import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 *
 * Holds any immutable MessageId, which in the PEPPOL world most probably
 * will be a globally unique UUID with or without a prefix of "uuid:".
 */
public class MessageId implements Serializable {

    private static final long serialVersionUID = -7667237415379267745L;

    private static final String REGEXP = "\\b(uuid:){0,1}\\s*([a-f0-9\\-]*){1}\\s*";
    private static final Pattern pattern = Pattern.compile(REGEXP);

    private String value;

    /**
     * Create a new MessageId
     * @param messageId any messageid represented as text
     */
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

    /**
     * Returns an UUID instance of the MessageId or throws exception if the contained format is wrong.
     * Note that the UUID instance will not have any "uuid:" prefix, if you need to preserve
     * the exact MessageId you should use the @see stringValue()
     * @throws IllegalStateException, IllegalArgumentException
     */
    public UUID toUUID() {
        return uuidFromStringWithOptionalPrefix(value);
    }

    private UUID uuidFromStringWithOptionalPrefix(String s) {
        Matcher matcher = pattern.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalStateException("Internal error in regexp. Unable to determine UUID of '" + s + "' using regexp " + REGEXP);
        } else {
            return UUID.fromString(matcher.group(2));
        }
    }

}
