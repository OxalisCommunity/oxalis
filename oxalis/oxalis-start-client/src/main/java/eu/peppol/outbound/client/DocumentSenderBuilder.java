package eu.peppol.outbound.client;

import eu.peppol.start.util.KeystoreManager;

import java.io.File;

/**
 * User: nigel
 * Date: Oct 24, 2011
 * Time: 10:38:35 AM
 */
@SuppressWarnings({"AccessStaticViaInstance"})
public class DocumentSenderBuilder {

    private File keystoreFile;
    private String keystorePassword;

    public PeppolDocumentSender build() {
        KeystoreManager keystoreManager = new KeystoreManager();
        keystoreManager.initialiseKeystore(keystoreFile, keystorePassword);

        return new PeppolDocumentSender();
    }

    public DocumentSenderBuilder setKeystoreFile(File keystore) {
        this.keystoreFile = keystore;
        return this;
    }

    public DocumentSenderBuilder setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
        return this;
    }
}
