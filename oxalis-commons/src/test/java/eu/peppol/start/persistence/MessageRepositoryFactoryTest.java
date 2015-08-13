/* Created by steinar on 24.06.12 at 22:09 */
package eu.peppol.start.persistence;

import eu.peppol.persistence.MessageRepository;
import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.util.ServiceLoader;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
@Test(groups = "manual")
public class MessageRepositoryFactoryTest {

    /**
     * Verifies that loading the plugable persistence implementation via a custom class loader, actually works
     * as expected.
     */
    @Test
    public void createClassLoader() throws MalformedURLException {

        String persistenceClassPath = GlobalConfiguration.getInstance().getPersistenceClassPath();

        ServiceLoader<MessageRepository> sl = MessageRepositoryFactory.createCustomServiceLoader(persistenceClassPath);
        assertNotNull(sl);

        int i = 0;
        for (MessageRepository messageRepository : sl) {
            i++;
        }
        assertTrue(i > 0, "No implementations found");
    }
}
