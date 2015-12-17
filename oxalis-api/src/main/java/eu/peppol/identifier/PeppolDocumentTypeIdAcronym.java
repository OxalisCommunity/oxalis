/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.identifier;

/**
 * Represents a PEPPOL Document Identifier acronym, textually represented thus:
 * <pre>
 *     &lt;root NS>::&lt;document element local name>##&lt;customization id>::&lt;version>
 * </pre>
 *
 * Provides short hand notation for PEPPOL Document Type Identifiers, which are otherwise fairly lengthy and complex.
 * This is just a simple helper class to make life easier :-)
 *
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 18:52
 * @see "PEPPOL Policy for us of Identifiers v2.2, POLICY 13"
 */
public enum PeppolDocumentTypeIdAcronym {



    // PEPPOL Catalogues (PEPPOL BIS profile 1a)
    PEPPOL_CATALOGUE(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2::Catalogue##urn:www.cenbii.eu:transaction:biicoretrdm019:ver1.0:#urn:www.peppol.eu:bis:peppol1a:ver1.0::2.0")),

    // Basic Order according to PEPPOL BIS 3a
    ORDER(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order##urn:www.cenbii.eu:transaction:biicoretrdm001:ver1.0:#urn:www.peppol.eu:bis:peppol3a:ver1.0::2.0")),

    // Standard PEPPOL BIS profile 4a invoice
    INVOICE(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0::2.1")),

    // EHF Invoice
    EHF_INVOICE(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0#urn:www.difi.no:ehf:faktura:ver1::2.0")),

    // Standalone Credit Note according to EHF
    EHF_CREDIT_NOTE(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.cenbii.eu:profile:biixx:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1::2.0")),

    // PEPPOL Billing (PEPPOL BIS Profile 5a)
    INVOICE_BILLING(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol5a:ver1.0::2.0")),

    // PEPPOL Billing (PEPPOL BIS Profile 5a)
    CREDIT_NOTE_BILLLING(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:bis:peppol5a:ver1.0::2.0")),

    // Credit invoice according to PEPPOL BIS 6a (Procurement)
    CREDIT_NOTE(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:bis:peppol6a:ver1.0::2.0")),

    // Tender (trdm090)
    TENDER(PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Tender-2::Tender##urn:www.cenbii.eu:transaction:biitrdm090:ver3.0::2.1"))

    ;

    private final static String scheme = "busdox-docid-qns";

    private final PeppolDocumentTypeId documentTypeIdentifier;

    PeppolDocumentTypeIdAcronym(PeppolDocumentTypeId identifier) {
        this.documentTypeIdentifier = identifier;
    }

    public static String getScheme() {
        return scheme;
    }

    @Override
    public String toString() {
        return documentTypeIdentifier.toString();
    }

    public PeppolDocumentTypeId getDocumentTypeIdentifier() {
        return documentTypeIdentifier;
    }
}
