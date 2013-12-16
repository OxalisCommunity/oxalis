package eu.peppol.outbound.api;

import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.security.KeystoreManager;
import eu.peppol.statistics.RawStatisticsRepository;
import eu.peppol.statistics.RawStatisticsRepositoryFactory;
import eu.peppol.statistics.RawStatisticsRepositoryFactoryProvider;

/**
 * Constructs a DocumentSender. A DocumentSender is dedicated to a particular document and process
 * type. DocumentSenders are guaranteed to be thread-safe.
 *
 * specification of
 * User: nigel
 * Date: Oct 24, 2011
 * Time: 10:38:35 AM
 *
 * @author Nigel Parker and Steinar Overbeck Cook
 * @deprecated since version 3.x, please use the TransmissionRequestBuilder and Transmitter
 *
 * @see eu.peppol.outbound.transmission.TransmissionRequestBuilder
 * @see eu.peppol.outbound.transmission.TransmissionRequest
 * @see eu.peppol.outbound.transmission.Transmitter
 */
@SuppressWarnings({"UnusedDeclaration"})

public class DocumentSenderBuilder {

    // Default document type identifier is EHF Invoice, TODO: remove this default value for document type identifier
    private PeppolDocumentTypeId documentTypeIdentifier = PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0#urn:www.difi.no:ehf:faktura:ver1::2.0");
    // Default value is set to BII4 TODO: remove default value for process type
    private PeppolProcessTypeId peppolProcessTypeId = PeppolProcessTypeId.valueOf("urn:www.cenbii.eu:profile:bii04:ver1.0");
    private boolean soapLogging;

    final public RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory;

    public DocumentSenderBuilder() {
        // Locates the statistics repository factory implementation in the class path
        rawStatisticsRepositoryFactory = RawStatisticsRepositoryFactoryProvider.getInstance();
    }

    /**
     * constructs and returns a DocumentSender based on the previously specified parameters.
     */
    public DocumentSender build() {
        KeystoreManager keystoreManager = KeystoreManager.getInstance();

        AccessPointIdentifier accessPointIdentifier = AccessPointIdentifier.valueOf(keystoreManager.getOurCommonName());

        RawStatisticsRepository rawStatisticsRepository = rawStatisticsRepositoryFactory.getInstanceForRawStatistics();
        return new DocumentSender(documentTypeIdentifier, peppolProcessTypeId, soapLogging, rawStatisticsRepository, accessPointIdentifier);
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
     * specifies the peppolProcessTypeId for the business process of which the document is a part. The default value is the
     * process containing a single invoice.
     */
    public DocumentSenderBuilder setPeppolProcessTypeId(PeppolProcessTypeId peppolProcessTypeId) {
        this.peppolProcessTypeId = peppolProcessTypeId;
        return this;
    }
}
