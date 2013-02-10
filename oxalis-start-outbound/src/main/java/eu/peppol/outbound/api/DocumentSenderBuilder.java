package eu.peppol.outbound.api;

import eu.peppol.start.identifier.*;
import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.StatisticsRepositoryFactory;
import eu.peppol.statistics.StatisticsRepositoryFactoryProvider;
import eu.peppol.util.GlobalConfiguration;

import java.io.File;
import java.util.ServiceLoader;

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

    private PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0#urn:www.difi.no:ehf:faktura:ver1::2.0");
    private PeppolProcessTypeId peppolProcessTypeId = PeppolProcessTypeId.valueOf("urn:www.cenbii.eu:profile:bii04:ver1.0");
    private File keystoreFile;
    private String keystorePassword;
    private boolean soapLogging;

    final public StatisticsRepositoryFactory  statisticsRepositoryFactory;

    public DocumentSenderBuilder() {
        // Locates the statistics repository factory implementation in the class path
        statisticsRepositoryFactory = StatisticsRepositoryFactoryProvider.getInstance();
    }

    /**
     * constructs and returns a DocumentSender based on the previously specified parameters.
     */
    public DocumentSender build() {
        KeystoreManager keystoreManager = new KeystoreManager();
        keystoreManager.initialiseKeystore(keystoreFile, keystorePassword);

        AccessPointIdentifier accessPointIdentifier = GlobalConfiguration.getInstance().getAccessPointIdentifier();

        StatisticsRepository statisticsRepository = statisticsRepositoryFactory.getInstance();
        return new DocumentSender(documentTypeIdentifier, peppolProcessTypeId, soapLogging, statisticsRepository, accessPointIdentifier);
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
