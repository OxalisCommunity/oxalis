package eu.peppol.identifier;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PeppolProcessTypeIdAcronymTest {

    @Test
    public void simple() {
        Assert.assertEquals(PeppolProcessTypeIdAcronym.valueOf("INVOICE_ONLY"), PeppolProcessTypeIdAcronym.INVOICE_ONLY);
        Assert.assertNotNull(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId());
        Assert.assertNotNull(PeppolProcessTypeIdAcronym.INVOICE_ONLY.toString());
    }
}
