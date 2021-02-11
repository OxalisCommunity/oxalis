/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.sniffer.identifier;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class DocumentTypeIdentifierTest {

    @Test
    public void testValueOf() {
        String documentIdAsText =
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol1a:ver1.0" +
                        "::2.0";
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeId.valueOf(documentIdAsText);
        assertEquals(documentTypeIdentifier.toString(), documentIdAsText);

        assertEquals(documentTypeIdentifier.getRootNameSpace(),
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2");
        assertEquals(documentTypeIdentifier.getLocalName(), "ApplicationResponse");
        assertEquals(documentTypeIdentifier.getCustomizationIdentifier(), CustomizationIdentifier.valueOf(
                "urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol1a:ver1.0"));
        assertEquals(documentTypeIdentifier.getVersion(), "2.0");
    }

    @Test
    public void equals() {
        String s = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                "##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0" +
                ":#urn:www.cenbii.eu:profile:biixx:ver1.0" +
                "#urn:www.difi.no:ehf:kreditnota:ver1" +
                "::2.0";
        String s2 = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                "##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0" +
                ":#urn:www.cenbii.eu:profile:biixx:ver1.0" +
                "#urn:www.difi.no:ehf:kreditnota:ver1" +
                "::3.0";

        PeppolDocumentTypeId d1 = PeppolDocumentTypeId.valueOf(s);
        PeppolDocumentTypeId d2 = PeppolDocumentTypeId.valueOf(s);

        PeppolDocumentTypeId d3 = PeppolDocumentTypeId.valueOf(s2);
        assertEquals(d1, d2);

        assertNotEquals(d1, d3);
    }

    /**
     * Verifies the Tender document
     */
    @Test
    public void tender() {
        PeppolDocumentTypeId tender = new PeppolDocumentTypeId(
                "urn:oasis:names:specification:ubl:schema:xsd:Tender-2",
                "Tender",
                new CustomizationIdentifier("urn:www.cenbii.eu:transaction:biitrdm090:ver3.0")
                , "2.1");
        assertEquals("urn:oasis:names:specification:ubl:schema:xsd:Tender-2::Tender" +
                "##urn:www.cenbii.eu:transaction:biitrdm090:ver3.0" +
                "::2.1", tender.toString());
    }

    @Test
    public void testLotsOfExpectedPeppolDocumentTypeIds() {

        // These are known to be in use and should be parsable without errors
        String[] documentIdentifiers = {
                // BIS and EHF
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol4a:ver1.0" +
                        "#urn:www.difi.no:ehf:faktura:ver1" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Reminder-2::Reminder" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm017:ver1.0" +
                        ":#urn:www.cenbii.eu:profile:biixy:ver1.0" +
                        "#urn:www.difi.no:ehf:purring:ver1" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm001:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol3a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol4a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm015:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol6a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0" +
                        ":#urn:www.cenbii.eu:profile:biixy:ver1.0" +
                        "#urn:www.difi.no:ehf:kreditnota:ver1" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponseSimple-2::OrderResponseSimple" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm003:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol6a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2::Catalogue" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm019:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol1a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:OrderResponseSimple-2::OrderResponseSimple" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm002:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol6a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm015:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol5a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0" +
                        ":#urn:www.cenbii.eu:profile:biixy:ver1.0" +
                        "#urn:www.difi.no:ehf:faktura:ver1" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0" +
                        ":#urn:www.cenbii.eu:profile:biixx:ver1.0" +
                        "#urn:www.difi.no:ehf:kreditnota:ver1" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol1a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Order-2::Order" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm001:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol6a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol6a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm058:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol1a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol6a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol5a:ver1.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0" +
                        ":#urn:www.peppol.eu:bis:peppol5a:ver1.0" +
                        "::2.0",
                // NESUBL
                "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote" +
                        "##NESUBL-2.0" +
                        "::2.0",
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##NESUBL-2.0" +
                        "::2.0",
                // OIOUBL
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice" +
                        "##OIOUBL-2.02" +
                        "::2.0"
        };

        for (String s : documentIdentifiers) {
            PeppolDocumentTypeId d = PeppolDocumentTypeId.valueOf(s);
            assertEquals(d.toString(), s);
        }
    }
}
