package eu.peppol.outbound.callbacks;

import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import com.sun.xml.wss.impl.callback.PrivateKeyCallback;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.util.KeystoreManager;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

/**
 * User: nigel
 * Date: Oct 18, 2011
 * Time: 4:59:37 PM
 */
public final class KeyStoreCallbackHandler implements CallbackHandler {

    public void handle(Callback[] callbacks) {
        KeystoreManager keystoreManager = new KeystoreManager();

        for (Callback callback : callbacks) {

            if (callback instanceof KeyStoreCallback) {

                Log.info("Returning keystore");
                KeyStoreCallback keyStoreCallback = (KeyStoreCallback) callback;
                keyStoreCallback.setKeystore(keystoreManager.getKeystore());

            } else if (callback instanceof PrivateKeyCallback) {

                Log.info("Returning private key");
                PrivateKeyCallback privateKeyCallback = (PrivateKeyCallback) callback;
                privateKeyCallback.setKey(keystoreManager.getOurPrivateKey());
            }
        }
    }
}
