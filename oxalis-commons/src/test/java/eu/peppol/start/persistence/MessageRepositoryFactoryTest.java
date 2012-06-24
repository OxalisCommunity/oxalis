/* Created by steinar on 24.06.12 at 22:09 */
package eu.peppol.start.persistence;

import org.testng.annotations.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ServiceLoader;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class MessageRepositoryFactoryTest {


    /** Verifies that loading the pluggable persistence implementation via a custom class loader, actually works
     * as expected.
     *
     */
    @Test
    public void createClassLoader() throws MalformedURLException {

        File d = new File("/Users/steinar/src/sr-peppol/aksesspunkt/oxalis-persistence/target/classes");
        if (d.exists()) {
            System.err.println(d.toURL());
            ServiceLoader<MessageRepository> sl = MessageRepositoryFactory.createCustomServiceLoader("file:///Users/steinar/src/sr-peppol/aksesspunkt/oxalis-persistence/target/classes/");
            assertNotNull(sl);

            int i = 0;
            for (MessageRepository messageRepository : sl) {
                i++;
            }

            assertTrue( i> 0, "No implementations found");
        }
    }
}
