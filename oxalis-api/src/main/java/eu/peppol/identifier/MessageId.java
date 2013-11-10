package eu.peppol.identifier;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steinar Overbeck Cook
 * 
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 18:44
 */
public class MessageId {

    public static final String REGEXP = "\\b(uuid:){0,1}\\s*([a-f0-9\\-]*){1}\\s*";
    String value;

    public static final Pattern pattern = Pattern.compile(REGEXP);

    /**
     * Holds an immutable instance of a UUID, which sometimes may be supplied as a string having a prefix of "urn:"
     *
     * @param messageId the UUID represented as text, with optional prefix of "urn:"
     */
    public MessageId(String messageId) {

        if (messageId == null) {
            throw new IllegalArgumentException("MessageId requires a non-null string");
        }

        uuidFromStringWithOptionalPrefix(messageId);

        value = messageId;
    }

    public String stringValue(){
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

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
