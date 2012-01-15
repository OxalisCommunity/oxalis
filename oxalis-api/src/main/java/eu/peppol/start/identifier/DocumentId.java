package eu.peppol.start.identifier;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 18:52
 */
public enum DocumentId {
    INVOICE("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0"),
    CREDIT_NOTE("urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:b is:peppol6a:ver1.0::2.0"),
    ORDER("urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biicoretrdm001:ver1.0:#urn:www.peppol.eu:bis:peppol3a:ver1.0::2.0"),
    ;

    private final static String scheme = "busdox-docid-qns";

    private final String identifier;

    private DocumentId(String identifier) {
        this.identifier = identifier;
    }

    /** Provides the enum corresponding to the supplied Peppol Document identifier */
    public static DocumentId valueFor(String documentIdentifier) {
        for (DocumentId documentId : values()) {
            if (documentId.identifier.equals(documentIdentifier)) {
                return documentId;
            }
        }

        return null;
    }

    public static String getScheme() {
        return scheme;
    }

    public String stringValue() {
        return toString();
    }

    @Override
    public String toString() {
        return identifier;
    }
}
