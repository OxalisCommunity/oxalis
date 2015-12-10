package eu.peppol;

import eu.peppol.util.GlobalConfigurationImpl;
import org.testng.annotations.BeforeSuite;

import java.io.File;

/**
 * Created by soc on 04.12.2015.
 */
public class BeforeSuiteTest {


    @BeforeSuite(groups = {"integration"})
    public void verifyKeysAndCertificates() throws Exception {
        String keyStoreFileName = new GlobalConfigurationImpl().getKeyStoreFileName();
        File file = new File(keyStoreFileName);
        if (!file.canRead()) {
            throw new IllegalStateException(keyStoreFileName + " does not exist or can not be read");
        }
    }
}
