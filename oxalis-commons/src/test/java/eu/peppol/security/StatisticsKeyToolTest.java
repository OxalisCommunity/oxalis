package eu.peppol.security;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 01.05.13
 *         Time: 21:36
 */
public class StatisticsKeyToolTest {

    private StatisticsKeyTool statisticsKeyTool;

    @BeforeClass
    public void createStatisticsKeyTool() {
        statisticsKeyTool = new StatisticsKeyTool();
    }


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

    @Test
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

    @Test(groups = {"integration"})
    public void loadPrivateKeyFromOxalisHome() throws Exception {
        PrivateKey privateKey = statisticsKeyTool.loadPrivateKeyFromOxalisHome();
        assertNotNull(privateKey);
    }

}
