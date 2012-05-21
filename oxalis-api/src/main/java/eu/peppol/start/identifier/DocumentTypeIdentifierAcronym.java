package eu.peppol.start.identifier;

/**
 * Represents a PEPPOL Document Identifier acronym, textually represented thus:
 * <pre>
 *     &lt;root NS>::&lt;document element local name>##&lt;customization id>::&lt;version>
 * </pre>
 *
 * Provides short hand notation for PEPPOL Document Type Identifiers, which are otherwise fairly lengthy and complex.
 *
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 18:52
 * @see "PEPPOL Policy for us of Identifiers v2.2, POLICY 13"
 */
public enum DocumentTypeIdentifierAcronym {

    // EHF Invoice
    EHF_INVOICE(DocumentTypeIdentifier.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0#urn:www.difi.no:ehf:faktura:ver1::2.0")),

    // Standard PEPPOL BIS profile 4a invoice
    INVOICE(DocumentTypeIdentifier.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0")),

    // Credit invoice according to PEPPOL BIS 5a (Billing)
    CREDIT_NOTE(DocumentTypeIdentifier.valueOf("urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:bis:peppol6a:ver1.0::2.0")),

    // Basic Order according to PEPPOL BIS 3a
    ORDER(DocumentTypeIdentifier.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biicoretrdm001:ver1.0:#urn:www.peppol.eu:bis:peppol3a:ver1.0::2.0")),
    ;

    private final static String scheme = "busdox-docid-qns";

    private final DocumentTypeIdentifier documentTypeIdentifier;

    DocumentTypeIdentifierAcronym(DocumentTypeIdentifier identifier) {
        this.documentTypeIdentifier = identifier;
    }

    public static String getScheme() {
        return scheme;
    }

    @Override
    public String toString() {
        return documentTypeIdentifier.toString();
    }

    public DocumentTypeIdentifier getDocumentTypeIdentifier() {
        return documentTypeIdentifier;
    }
}
