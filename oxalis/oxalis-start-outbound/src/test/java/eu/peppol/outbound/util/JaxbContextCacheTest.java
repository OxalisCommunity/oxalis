package eu.peppol.outbound.util;

import org.testng.annotations.Test;

import javax.xml.bind.JAXBContext;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:05:52 AM
 */
@Test
public class JaxbContextCacheTest extends TestBase {

    public void test01() throws Throwable {
        try {

            JAXBContext context1 = JaxbContextCache.getInstance(String.class);
            JAXBContext context2 = JaxbContextCache.getInstance(String.class);
            assertTrue(context1 == context2);

        } catch (Throwable t) {
            signal(t);
        }
    }
}
