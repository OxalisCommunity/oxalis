/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.commons.bouncycastle;

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
