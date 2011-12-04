package eu.peppol.start.identifier;

import java.security.PublicKey;

/**
 * @author Steinar Overbeck Cook
 * 
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 18:48
 */
public class ParticipantId {
    String value;

    public static String getScheme() {
        return scheme;
    }

    private static final String scheme = "iso6523-actorid-upis";

    public ParticipantId(String recipientId) {
        if (recipientId == null) {
            throw new IllegalArgumentException("ParticipantId requires a non-null argument");
        }
        value=recipientId;
    }

    public String stringValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }


}
