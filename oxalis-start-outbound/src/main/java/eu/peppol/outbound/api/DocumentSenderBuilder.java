package eu.peppol.outbound.api;

import eu.peppol.start.identifier.*;

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

    private PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier();
    private PeppolProcessTypeId peppolProcessTypeId = PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId();
    private File keystoreFile;
    private String keystorePassword;
    private boolean soapLogging;

    /**
     * constructs and returns a DocumentSender based on the previously specified parameters.
     */
    public DocumentSender build() {
        KeystoreManager keystoreManager = new KeystoreManager();
        keystoreManager.initialiseKeystore(keystoreFile, keystorePassword);

        return new DocumentSender(documentTypeIdentifier, peppolProcessTypeId, soapLogging);
    }

    /**
     * enables logging of SOAP messages. The default is eu logging.
     */
    public DocumentSenderBuilder enableSoapLogging() {
        this.soapLogging = true;
        return this;
    }

    /**
     * sets the document type for this DocumentSender. The default value is an invoice document.
     */
    public DocumentSenderBuilder setDocumentTypeIdentifier(PeppolDocumentTypeId documentTypeIdentifier) {
        this.documentTypeIdentifier = documentTypeIdentifier;
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
     * specifies the peppolProcessTypeId for the business process of which the document is a part. The default value is the
     * process containing a single invoice.
     */
    public DocumentSenderBuilder setPeppolProcessTypeId(PeppolProcessTypeId peppolProcessTypeId) {
        this.peppolProcessTypeId = peppolProcessTypeId;
        return this;
    }
}
