package eu.peppol.security.callbacks;

import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import com.sun.xml.wss.impl.callback.PrivateKeyCallback;
import eu.peppol.start.identifier.KeystoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;
import java.security.PrivateKey;

/**
 * User: nigel
 * Date: Oct 18, 2011
 * Time: 4:59:37 PM
 */
public final class KeyStoreCallbackHandler implements CallbackHandler {

    public static final Logger log = LoggerFactory.getLogger(KeyStoreCallbackHandler.class);

    public void handle(Callback[] callbacks) {
        KeystoreManager keystoreManager = new KeystoreManager();

        for (Callback callback : callbacks) {

            if (callback instanceof KeyStoreCallback) {

                log.debug("Keystore callback handler: returning keystore");
                KeyStore keystore = keystoreManager.getKeystore();
                ((KeyStoreCallback) callback).setKeystore(keystore);

            } else if (callback instanceof PrivateKeyCallback) {

                log.debug("Keystore callback handler: returning private key");
                PrivateKey privateKey = keystoreManager.getOurPrivateKey();
                ((PrivateKeyCallback) callback).setKey(privateKey);
            }
        }
    }
}
