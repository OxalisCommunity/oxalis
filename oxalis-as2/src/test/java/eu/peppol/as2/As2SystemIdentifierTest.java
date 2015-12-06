package eu.peppol.as2;

import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 *         Date: 09.10.13
 *         Time: 10:41
 */
@Test(groups = {"soc"})
public class As2SystemIdentifierTest {

    @Test(groups = {"soc"})
    public void testToString() throws Exception {

        As2SystemIdentifier ap_0001 = new As2SystemIdentifier("AP_0001");
        assertEquals(ap_0001.toString(), "AP_0001");
    }

    @Test(expectedExceptions = InvalidAs2SystemIdentifierException.class)
    public void testInvalidAs2Name() throws Exception {

        As2SystemIdentifier as2SystemIdentifier = new As2SystemIdentifier("\\ap32");
    }

    public void testEquals() throws Exception {

        As2SystemIdentifier first = new As2SystemIdentifier("AP_00032");
        As2SystemIdentifier second = new As2SystemIdentifier("AP_00032");
        assertEquals(first,second);

        assertEquals(first.hashCode(), second.hashCode());
    }
}
