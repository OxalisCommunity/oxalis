package eu.peppol.outbound.util;

import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: nigel
 * Date: Oct 16, 2011
 * Time: 9:29:54 PM
 */
public class Identifiers {

    public static DocumentIdentifierType INVOICE;
    public static ProcessIdentifierType INVOICE_PROCESS;

    private static String DOCUMENT_SCHEME = "busdox-docid-qns";
    private static String INVOICE_VALUE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0";
    private static String PROCESS_SCHEME = "cenbii-procid-ubl";
    private static String PROCESS_VALUE = "urn:www.cenbii.eu:profile:bii04:ver1.0";
    private static String PARTICIPANT_SCHEME = "iso6523-actorid-upis";
    private static String NO_AGENCY_CODE_NO_VAT = "9908";
    private static String NO_AGENCY_CODE_VAT = "9909";

    public static synchronized DocumentIdentifierType getInvoiceDocumentIdentifier() {
        if (INVOICE == null) {
            INVOICE = new DocumentIdentifierType();
            INVOICE.setScheme(DOCUMENT_SCHEME);
            INVOICE.setValue(INVOICE_VALUE);
        }

        return INVOICE;
    }

    public static synchronized ProcessIdentifierType getInvoiceProcessIdentifier() {
        if (INVOICE_PROCESS == null) {
            INVOICE_PROCESS = new ProcessIdentifierType();
            INVOICE_PROCESS.setScheme(PROCESS_SCHEME);
            INVOICE_PROCESS.setValue(PROCESS_VALUE);
        }

        return INVOICE_PROCESS;
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

    public static ParticipantIdentifierType getParticipantIdentifier(String value) {

        ParticipantIdentifierType participantIdentifierType = new ParticipantIdentifierType();
        participantIdentifierType.setScheme(PARTICIPANT_SCHEME);
        participantIdentifierType.setValue(value);
        return participantIdentifierType;
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

        int calculatedCheckDigit = 11 - sum % 11;
        return actualCheckDigit == calculatedCheckDigit;
    }
}
