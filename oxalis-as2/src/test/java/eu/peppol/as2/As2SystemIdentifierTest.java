package eu.peppol.as2;

import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 10:41
 */
public class As2SystemIdentifierTest {

    @Test
    public void testToString() throws Exception {

        As2SystemIdentifier ap_0001 = new As2SystemIdentifier("AP_0001");
        assertEquals(ap_0001.toString(), "AP_0001");
    }

    @Test(expectedExceptions = As2SystemIdentifier.InvalidAs2SystemIdentifierException.class)
    public void testInvalidAs2Name() throws Exception {

        As2SystemIdentifier as2SystemIdentifier = new As2SystemIdentifier("\\ap32");
    }

    @Test
    public void testEquals() throws Exception {

        As2SystemIdentifier first = new As2SystemIdentifier("AP_00032");
        As2SystemIdentifier second = new As2SystemIdentifier("AP_00032");
        assertEquals(first,second);

        assertEquals(first.hashCode(), second.hashCode());
    }

    /** Creating an As2SystemIdentifier from an X500Principal object, should result in the value of the CN attribute */
    @Test
    public void as2SystemIdentifierFromCertificate() throws Exception {

        X500Principal x500Principal = new X500Principal("CN=PEPPOL ACCESS POINT CA, O=NATIONAL IT AND TELECOM AGENCY, C=DK");
        As2SystemIdentifier as2SystemIdentifier = new As2SystemIdentifier(x500Principal);
        assertEquals(as2SystemIdentifier.toString(), "PEPPOL ACCESS POINT CA");
    }

    @Test(expectedExceptions = As2SystemIdentifier.InvalidAs2SystemIdentifierException.class)
    public void testInvalidX500Principal() throws Exception {
        X500Principal x500Principal = new X500Principal("O=National and dummy");
        new As2SystemIdentifier(x500Principal);

    }
}
