package eu.peppol.outbound.callbacks;

import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.util.KeystoreManager;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

/**
 * User: nigel
 * Date: Oct 18, 2011
 * Time: 4:59:37 PM
 */
public final class TrustStoreCallbackHandler implements CallbackHandler {

    public void handle(Callback[] callbacks) {
        KeystoreManager keystoreManager = new KeystoreManager();

        for (Callback callback : callbacks) {

            if (callback instanceof KeyStoreCallback) {
                Log.info("Returning truststore");
                KeyStoreCallback keyStoreCallback = (KeyStoreCallback) callback;
                keyStoreCallback.setKeystore(keystoreManager.getTruststore());
            }
        }
    }
}
