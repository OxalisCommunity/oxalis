package eu.peppol.start.identifier;

import org.testng.annotations.Test;

import java.security.Principal;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
/**
 * @author steinar
 *         Date: 31.05.13
 *         Time: 14:11
 */
public class StartMessageHeaderTest {
    @Test
    public void testToString() throws Exception {

        StartMessageHeader startMessageHeader = new StartMessageHeader();

        try {
            startMessageHeader.toString();
        } catch (Throwable t) {
            fail("toString() method fails; " + t.getMessage());
            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testOutput() throws Exception {
        StartMessageHeader p = new StartMessageHeader();
        p.setRemoteAccessPointPrincipal(new Principal() {
            @Override
            public String getName() {
                return "test";
            }
        });

        String s = p.toString();
        assertTrue(s.contains("test"), "Invalid response from toString()" + s);
    }
}
