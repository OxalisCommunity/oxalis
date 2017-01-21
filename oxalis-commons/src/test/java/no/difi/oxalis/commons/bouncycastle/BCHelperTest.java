package no.difi.oxalis.commons.bouncycastle;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

/**
 * @author erlend
 */
public class BCHelperTest {

    @Test
    public void simpleConstructor() {
        new BCHelper();
    }

    @Test
    public void simpleRegisterProvider() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null)
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);

        Assert.assertNull(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME));

        BCHelper.registerProvider();

        Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        Assert.assertNotNull(provider);

        BCHelper.registerProvider();

        Assert.assertTrue(provider == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME));
    }

    @Test
    public void createMessageDigest() throws Exception {
        BCHelper.registerProvider();

        Assert.assertNotNull(BCHelper.getMessageDigest("SHA-1"));
    }

    @Test(expectedExceptions = NoSuchAlgorithmException.class)
    public void triggerExceptionWhenProviderIsNotFound() throws Exception {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);

        BCHelper.getMessageDigest("SHA-512");
    }
}
