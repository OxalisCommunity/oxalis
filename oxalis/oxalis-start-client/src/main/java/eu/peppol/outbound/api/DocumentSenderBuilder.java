package eu.peppol.outbound.api;

import eu.peppol.outbound.util.Identifiers;
import eu.peppol.start.util.KeystoreManager;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

import java.io.File;

/**
 * User: nigel
 * Date: Oct 24, 2011
 * Time: 10:38:35 AM
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DocumentSenderBuilder {

    private DocumentIdentifierType documentId = Identifiers.getInvoiceDocumentIdentifier();
    private ProcessIdentifierType processId = Identifiers.getInvoiceProcessIdentifier();
    private File keystoreFile;
    private String keystorePassword;
    private boolean soapLogging;

    public DocumentSender build() {
        KeystoreManager keystoreManager = new KeystoreManager();
        keystoreManager.initialiseKeystore(keystoreFile, keystorePassword);

        return new DocumentSender(documentId, processId, soapLogging);
    }

    public DocumentSenderBuilder enableSoapLogging() {
        this.soapLogging = true;
        return this;
    }

    public DocumentSenderBuilder setDocumentId(DocumentIdentifierType documentId) {
        this.documentId = documentId;
        return this;
    }

    public DocumentSenderBuilder setKeystoreFile(File keystore) {
        this.keystoreFile = keystore;
        return this;
    }

    public DocumentSenderBuilder setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
        return this;
    }

    public DocumentSenderBuilder setProcessId(ProcessIdentifierType processId) {
        this.processId = processId;
        return this;
    }

}
