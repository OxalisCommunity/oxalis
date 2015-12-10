/* Created by steinar on 14.05.12 at 00:10 */
package eu.peppol.security;

import com.google.inject.Inject;
import eu.peppol.util.RuntimeConfigurationModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
@Test(groups = {"integration"})
@Guice(modules = {RuntimeConfigurationModule.class, SecurityModule.class})
public class KeystoreManagerIT {

    @Inject KeystoreManager keystoreManager;

    @Test
    public void loadKeystore() throws Exception {

        assertNotNull(keystoreManager);

    }
}
