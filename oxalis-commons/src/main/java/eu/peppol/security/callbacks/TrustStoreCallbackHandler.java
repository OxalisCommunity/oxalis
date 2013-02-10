package eu.peppol.security.callbacks;

import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import eu.peppol.start.identifier.KeystoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import java.security.KeyStore;

/**
 * User: nigel
 * Date: Oct 18, 2011
 * Time: 4:59:37 PM
 */
public final class TrustStoreCallbackHandler implements CallbackHandler {

    public static final Logger log = LoggerFactory.getLogger(TrustStoreCallbackHandler.class);

    public void handle(Callback[] callbacks) {
        KeystoreManager keystoreManager = new KeystoreManager();

        for (Callback callback : callbacks) {

            if (callback instanceof KeyStoreCallback) {
                log.debug("Returning truststore");
                KeyStore truststore = keystoreManager.getTruststore();
                ((KeyStoreCallback) callback).setKeystore(truststore);
            }
        }
    }
}
