package eu.peppol.outbound.api;

import eu.peppol.outbound.util.Identifiers;
import eu.peppol.start.util.KeystoreManager;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

import java.io.File;

/**
 * responsible for constructing a DocumentSender. A DocumentSender is dedicated to a particular document and process
 * type. DocumentSenders are guaranteed to be thread-safe.
 *
 * specification of
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

    /**
     * constructs and returns a DocumentSender based on the previously specified parameters.
     */
    public DocumentSender build() {
        KeystoreManager keystoreManager = new KeystoreManager();
        keystoreManager.initialiseKeystore(keystoreFile, keystorePassword);

        return new DocumentSender(documentId, processId, soapLogging);
    }

    /**
     * enables logging of SOAP messages. The default is no logging.
     */
    public DocumentSenderBuilder enableSoapLogging() {
        this.soapLogging = true;
        return this;
    }

    /**
     * sets the document type for this DocumentSender. The default value is an invoice document.
     */
    public DocumentSenderBuilder setDocumentId(DocumentIdentifierType documentId) {
        this.documentId = documentId;
        return this;
    }

    /**
     * specifies the location of the keystore containing our own certificate and private key.
     */
    public DocumentSenderBuilder setKeystoreFile(File keystore) {
        this.keystoreFile = keystore;
        return this;
    }

    /**
     * specifies the password for the keystore.
     */
    public DocumentSenderBuilder setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
        return this;
    }

    /**
     * specifies the processId for the business process of which the document is a part. The default value is the
     * process containing a single invoice.
     */
    public DocumentSenderBuilder setProcessId(ProcessIdentifierType processId) {
        this.processId = processId;
        return this;
    }
}
