package eu.peppol.identifier;

import java.io.Serializable;
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
 *
 * TODO: introduce the iso6235 ICD as a separate property of the constructor
 *
 * @see SchemeId
 */
public class ParticipantId implements Serializable {

    private static String NO_AGENCY_CODE_NO_VAT = "9908";
    private static String NO_AGENCY_CODE_VAT = "9909";

    // The weight array obtained from Brønnøysund, used to validate a norwegian org no
    static final Integer[] ORG_NO_WEIGHT = new Integer[]{3, 2, 7, 6, 5, 4, 3, 2};
    static final int MODULUS_11 = 11;

    private final String value;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParticipantId that = (ParticipantId) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
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

        // Special check for Norwegian organisation numbers
        if (agencyCode.equals(NO_AGENCY_CODE_NO_VAT) || agencyCode.equals(NO_AGENCY_CODE_VAT)) {
            String organisationNumber = matcher.group(2);
            return isValidOrganisationNumber(organisationNumber);
        } else {
            return true;
        }
    }

    public static boolean isValidOrganisationNumber(String org) {
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

        int modulus = sum % 11;

        /** don't subtract from length if the modulus is 0 */
        if ((modulus == 0) && (actualCheckDigit == 0)) {
            return true;
        }

        int calculatedCheckDigit = 11 - modulus;
        return actualCheckDigit == calculatedCheckDigit;
    }

}
