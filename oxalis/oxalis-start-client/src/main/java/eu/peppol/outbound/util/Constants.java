package eu.peppol.outbound.util;

import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

/**
 * User: nigel
 * Date: Oct 16, 2011
 * Time: 9:29:54 PM
 */
public class Constants {

    public static DocumentIdentifierType INVOICE;
    public static ProcessIdentifierType INVOICE_PROCESS;

    private static String DOCUMENT_SCHEME = "busdox-docid-qns";
    private static String INVOICE_VALUE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0";
    private static String PROCESS_SCHEME = "cenbii-procid-ubl";
    private static String PROCESS_VALUE = "urn:www.cenbii.eu:profile:bii04:ver1.0";
    private static String PARTICIPANT_SCHEME = "iso6523-actorid-upis";

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

    public static ParticipantIdentifierType getParticipantIdentifier(String value) {

        ParticipantIdentifierType participantIdentifierType = new ParticipantIdentifierType();
        participantIdentifierType.setScheme(PARTICIPANT_SCHEME);
        participantIdentifierType.setValue(value);
        return participantIdentifierType;
    }
}
