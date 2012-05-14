/* Created by steinar on 14.05.12 at 00:10 */
package eu.peppol.start.identifier;

import org.testng.annotations.Test;

import java.security.cert.TrustAnchor;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
public class KeystoreManagerTest {
    @Test
    public void testGetTruststore() throws Exception {
        KeystoreManager km = new KeystoreManager();
        TrustAnchor trustAnchor = km.getTrustAnchor();
        System.out.println(trustAnchor.getTrustedCert().getSubjectDN());
    }


}
