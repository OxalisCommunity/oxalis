package eu.peppol.start.identifier;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 18:48
 */
public class ParticipantId {
    private static String NO_AGENCY_CODE_NO_VAT = "9908";
    private static String NO_AGENCY_CODE_VAT = "9909";

    private String value;

    public static String getScheme() {
        return scheme;
    }

    private static final String scheme = "iso6523-actorid-upis";

    public ParticipantId(String recipientId) {
        if (recipientId == null) {
            throw new IllegalArgumentException("ParticipantId requires a non-null argument");
        }
        value = recipientId;
    }

    public String stringValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static boolean isValidParticipantIdentifier(String value) {
        if (value == null) {
            return false;
        }

        Pattern pattern = Pattern.compile("(\\d{4}):(.+)");
        Matcher matcher = pattern.matcher(value);

        if (!matcher.find()) {
            return false;
        }

        String agencyCode = matcher.group(1);

        if (agencyCode.equals(NO_AGENCY_CODE_NO_VAT) || agencyCode.equals(NO_AGENCY_CODE_VAT)) {
            String organisationNumber = matcher.group(2);
            return isValidOrganisationNumber(organisationNumber);
        } else {
            return true;
        }
    }

    static boolean isValidOrganisationNumber(String org) {
        if (org == null || org.length() != 9 || !Character.isDigit(org.charAt(8))) {
            return false;
        }

        int actualCheckDigit = org.charAt(8) - 48;
        List<Integer> weights = Arrays.asList(3, 2, 7, 6, 5, 4, 3, 2);
        int sum = 0;

        for (int i = 0; i < 8; i++) {
            char next = org.charAt(i);

            if (!Character.isDigit(next)) {
                return false;
            }

            int digit = (int) next - 48;
            sum += digit * weights.get(i);
        }

        int calculatedCheckDigit = 11 - sum % 11;
        return actualCheckDigit == calculatedCheckDigit;
    }
}
