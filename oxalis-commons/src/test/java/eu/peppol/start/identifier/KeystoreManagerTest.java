/* Created by steinar on 14.05.12 at 00:10 */
package eu.peppol.start.identifier;

import org.testng.annotations.Test;

import java.security.cert.TrustAnchor;

import static org.testng.Assert.assertNotNull;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class KeystoreManagerTest {

    @Test(groups = "integration")
    public void testGetTruststore() throws Exception {

        KeystoreManager km = KeystoreManager.getInstance();
        assertNotNull(km.getOurKeystore(),"Our keystore was not loaded");
        assertNotNull(km.getPeppolTruststore(), "The PEPPOL trustore was not loaded from " + km.trustStoreResource());

        assertNotNull(km.getOurPrivateKey(), "Our private key was not available");
        assertNotNull(km.getOurCertificate(),"Our certificate was not available");

        TrustAnchor trustAnchor = km.getTrustAnchor();
        System.out.println(trustAnchor.getTrustedCert().getSubjectDN());

        System.out.println(km.getOurCertificate().getSerialNumber()+"");
    }


}
