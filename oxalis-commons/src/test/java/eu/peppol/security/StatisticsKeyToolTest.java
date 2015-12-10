package eu.peppol.security;

import com.google.inject.Inject;
import eu.peppol.util.RuntimeConfigurationModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Verifies the handling of the statistics public and private keys.
 *
 * @author steinar
 *         Date: 01.05.13
 *         Time: 21:36
 */
@Guice(modules = {RuntimeConfigurationModule.class, SecurityModule.class})
public class StatisticsKeyToolTest {

    @Inject
    StatisticsKeyTool statisticsKeyTool;

    /** Verifies my understanding of key factories */
    @Test
    public void recreatePublicKey() throws Exception {
        KeyPair keyPair = statisticsKeyTool.createKeyPair();

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()));

        assertEquals(publicKey, keyPair.getPublic());
    }

    @Test
    public void testCreateSaveAndLoadPublicKey() throws Exception {

        KeyPair keyPair = statisticsKeyTool.createKeyPair();
        statisticsKeyTool.saveKeyPair(keyPair);
        PublicKey publicKey = statisticsKeyTool.loadPublicKey(statisticsKeyTool.getPublicKeyFile());
        assertEquals(keyPair.getPublic(), publicKey);
    }

    @Test(groups = {"difi", "integration"})
    public void testCreateSaveAndLoadPrivateKey() throws Exception {
        KeyPair keyPair = statisticsKeyTool.createKeyPair();
        statisticsKeyTool.saveKeyPair(keyPair);

        PrivateKey privateKey = statisticsKeyTool.loadPrivateKey(statisticsKeyTool.getPrivateKeyFile());

        assertEquals(privateKey, keyPair.getPrivate());
    }


    @Test
    public void loadPublicKeyFromClassPath() {
        PublicKey publicKey = statisticsKeyTool.loadPublicKeyFromClassPath();
        assertNotNull(publicKey);
    }

    @Test(groups = {"difi","integration"})
    public void loadPrivateKeyFromOxalisHome() throws Exception {
        PrivateKey privateKey = statisticsKeyTool.loadPrivateKeyFromOxalisHome();
        assertNotNull(privateKey);
    }

}
