package eu.peppol;

import eu.peppol.util.GlobalConfiguration;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by soc on 04.12.2015.
 */
public class BeforeSuiteTest {

    @BeforeSuite(groups = {"integration"})
    public void verifyKeysAndCertificates() throws Exception {
        String keyStoreFileName = GlobalConfiguration.getInstance().getKeyStoreFileName();
        File file = new File(keyStoreFileName);
        if (!file.canRead()) {
            throw new IllegalStateException(keyStoreFileName + " does not exist or can not be read");
        }
    }
}
