package eu.peppol.identifier;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PeppolDocumentTypeIdAcronymTest {

    @Test
    public void simple() {
        Assert.assertEquals(PeppolDocumentTypeIdAcronym.valueOf("INVOICE"), PeppolDocumentTypeIdAcronym.INVOICE);
        Assert.assertNotNull(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        Assert.assertNotNull(PeppolDocumentTypeIdAcronym.INVOICE.toString());
    }
}
