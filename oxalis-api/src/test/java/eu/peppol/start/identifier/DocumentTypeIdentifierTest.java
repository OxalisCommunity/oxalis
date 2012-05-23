/* Created by steinar on 20.05.12 at 14:24 */
package eu.peppol.start.identifier;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class DocumentTypeIdentifierTest {

    @Test
    public void testValueOf() throws Exception {
        String documentIdAsText = "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2::ApplicationResponse##urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0:#urn:www.peppol.eu:bis:peppol1a:ver1.0::2.0";
        PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeId.valueOf(documentIdAsText);
        assertEquals(documentTypeIdentifier.toString(), documentIdAsText);

        assertEquals(documentTypeIdentifier.getRootNameSpace(),"urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2");
        assertEquals(documentTypeIdentifier.getLocalName(), "ApplicationResponse");
        assertEquals(documentTypeIdentifier.getCustomizationIdentifier(), CustomizationIdentifier.valueOf("urn:www.cenbii.eu:transaction:biicoretrdm057:ver1.0:#urn:www.peppol.eu:bis:peppol1a:ver1.0"));
        assertEquals(documentTypeIdentifier.getVersion(),"2.0");
    }

    @Test
    public void equals() {
        String s = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.cenbii.eu:profile:biixx:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1::2.0";
        String s2 = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2::CreditNote##urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.cenbii.eu:profile:biixx:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1::3.0";

        PeppolDocumentTypeId d1 = PeppolDocumentTypeId.valueOf(s);
        PeppolDocumentTypeId d2 = PeppolDocumentTypeId.valueOf(s);

        PeppolDocumentTypeId d3 = PeppolDocumentTypeId.valueOf(s2);
        assertEquals(d1,d2);

        assertNotEquals(d1,d3);
    }
}
