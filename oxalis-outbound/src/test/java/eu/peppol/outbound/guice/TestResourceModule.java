package eu.peppol.outbound.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.CustomizationIdentifier;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.outbound.transmission.TransmissionTestModule;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;

/**
 * Guice module providing different testfiles used by unit and integration tests
 *
 * @author steinar
 * @author thore
 */
public class TestResourceModule extends AbstractModule {

    public static final String PEPPOL_BIS_INVOICE_SBD_XML = "peppol-bis-invoice-sbdh.xml";
    public static final String EHF_T10_ALLE_ELEMENTER_XML = "ehf-t10-alle-elementer.xml";

    @Override
    protected void configure() { /* nothing */ }

    /**
     * All InputStream annotated with
     * <code>@Inject @Named("sampleXml")</code>, will have an instance of this InputStream injected.
     *
     * @return InputStream connected to PEPPOL_BIS_INVOICE_SBD_XML
     */
    @Provides
    @Named("sampleXml")
    public InputStream getSampleXmlInputStream() {
        InputStream resourceAsStream = TransmissionTestModule.class.getClassLoader().getResourceAsStream(PEPPOL_BIS_INVOICE_SBD_XML);
        assertNotNull(resourceAsStream, "Unable to load " + PEPPOL_BIS_INVOICE_SBD_XML + " from class path");
        return resourceAsStream;
    }

    @Provides
    @Named("no-sbdh-xml")
    public InputStream getSampleXmlInputStreamWithoutSbdh() {
        InputStream inputStream = TransmissionTestModule.class.getClassLoader().getResourceAsStream(EHF_T10_ALLE_ELEMENTER_XML);
        assertNotNull(inputStream, "Unable to load " + EHF_T10_ALLE_ELEMENTER_XML + " from class path");
        return inputStream;
    }

    /**
     * Provides a Map of resource names and their PeppolStandardBusinessHeader "facit".
     * Extend the number of testfiles and update this list to automatically test decoding of new formats.
     */
    @Provides
    @Named("test-files-with-identification")
    public Map<String, PeppolStandardBusinessHeader> getTestData() {
        Map<String, PeppolStandardBusinessHeader> map = new HashMap<String, PeppolStandardBusinessHeader>();

        //
        // example Catalogue scenario files
        //

        map.put("EHFCatalogue/1.0/Example file EHF Catalogue Response.xml", createPeppolStandardBusinessHeader(
            "ApplicationResponse", "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2", "2.1",
            "urn:www.cenbii.eu:transaction:biitrns058:ver2.0:extended:urn:www.peppol.eu:bis:peppol1a:ver2.0:extended:urn:www.difi.no:ehf:katalogbekreftelse:ver1.0",
            "9908:1234567890", "9908:123456789", "urn:www.cenbii.eu:profile:biiI02:ver2.0"));

        map.put("EHFCatalogue/1.0/Example file EHF Catalogue.xml", createPeppolStandardBusinessHeader(
            "Catalogue", "urn:oasis:names:specification:ubl:schema:xsd:Catalogue-2", "2.1",
            "urn:www.cenbii.eu:transaction:biitrns019:ver2.0:extended:urn:www.peppol.eu:bis:peppol1a:ver2.0:extended:urn:www.difi.no:ehf:katalog:ver1.0",
            "9908:1234567890", "9908:123456789", "urn:www.cenbii.eu:profile:bii01:ver2.0"));

        //
        // example Invoice scenario files
        //

        map.put("EHFInvoice/1.6/T14-norsk-profil05.xml", createPeppolStandardBusinessHeader(
            "CreditNote", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2", "2.0",
            "urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:bis:peppol5a:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1",
            "9908:977187761", "9908:810305282", "urn:www.cenbii.eu:profile:bii05:ver1.0"));

        map.put("EHFInvoice/1.6/T14-norsk-profilxx.xml", createPeppolStandardBusinessHeader(
            "CreditNote", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2", "2.0",
            "urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:bis:peppol5a:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1",
            "9908:977187761", "9908:810305282", "urn:www.cenbii.eu:profile:biixx:ver1.0"));

        map.put("EHFInvoice/1.6/T14-norsk-profilxy.xml", createPeppolStandardBusinessHeader(
            "CreditNote", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2", "2.0",
            "urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.cenbii.eu:profile:biixy:ver1.0#urn:www.difi.no:ehf:kreditnota:ver1",
            "9908:977187761", "9908:810305282", "urn:www.cenbii.eu:profile:biixy:ver1.0"));

        map.put("EHFInvoice/1.6/T14-utland-profil05.xml", createPeppolStandardBusinessHeader(
            "CreditNote", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2", "2.0",
            "urn:www.cenbii.eu:transaction:biicoretrdm014:ver1.0:#urn:www.peppol.eu:bis:peppol5a:ver1.0",
            "9908:977187761", "9908:810305282", "urn:www.cenbii.eu:profile:bii05:ver1.0"));

        map.put("EHFInvoice/2.0/T10-B2C.xml", createPeppolStandardBusinessHeader(
            "Invoice", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2", "2.1",
            "urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0",
            "9908:123456789", "9908:987489712", "urn:www.cenbii.eu:profile:bii05:ver2.0"));

        map.put("EHFInvoice/2.0/T10-Valuta-EUR.xml", createPeppolStandardBusinessHeader(
            "Invoice", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2", "2.1",
            "urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:urn:www.difi.no:ehf:faktura:ver2.0",
            "9908:123456789", "9908:987654321", "urn:www.cenbii.eu:profile:bii05:ver2.0"));

        map.put("EHFInvoice/2.0/T14-Valuta-EUR.xml", createPeppolStandardBusinessHeader(
            "CreditNote", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2", "2.1",
            "urn:www.cenbii.eu:transaction:biitrns014:ver2.0:extended:urn:www.peppol.eu:bis:peppol5a:ver2.0:extended:urn:www.difi.no:ehf:kreditnota:ver2.0",
            "9908:123456789", "9908:987654321", "urn:www.cenbii.eu:profile:bii05:ver2.0"));

        //
        // example Order scenario files
        //

        map.put("EHFOrder/1.0/Eksempelfil EHF Ordre.xml", createPeppolStandardBusinessHeader(
                "Order", "urn:oasis:names:specification:ubl:schema:xsd:Order-2", "2.1",
                "urn:www.cenbii.eu:transaction:biitrns001:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0:extended:urn:www.difi.no:ehf:ordre:ver1.0",
                "9908:931186755", "9908:938752655", "urn:www.cenbii.eu:profile:bii28:ver2.0"));

        map.put("EHFOrder/1.0/Eksempelfil EHF Ordrebekreftelse.xml", createPeppolStandardBusinessHeader(
                "OrderResponse", "urn:oasis:names:specification:ubl:schema:xsd:OrderResponse-2", "2.1",
                "urn:www.cenbii.eu:transaction:biitrns076:ver2.0:extended:urn:www.peppol.eu:bis:peppol28a:ver1.0:extended:urn:www.difi.no:ehf:ordrebekreftelse:ver1.0",
                "9908:123456789", "9908:931186755", "urn:www.cenbii.eu:profile:bii28:ver2.0"));

        //
        // example Reminder scenario files
        //

        map.put("EHFReminder/1.6/T17-norsk-profilxy.xml", createPeppolStandardBusinessHeader(
                "Reminder", "urn:oasis:names:specification:ubl:schema:xsd:Reminder-2", "2.0",
                "urn:www.cenbii.eu:transaction:biicoretrdm017:ver1.0:#urn:www.cenbii.eu:profile:biixy:ver1.0#urn:www.difi.no:ehf:puring:ver1",
                "9908:123456789", "9908:123456798", "urn:www.cenbii.eu:profile:biixy:ver1.0"));

        return map;
    }

    private PeppolStandardBusinessHeader createPeppolStandardBusinessHeader(
            String localname, String namespace, String version,
            String customization,
            String sender, String receiver, String profileId) {
        PeppolStandardBusinessHeader p = new PeppolStandardBusinessHeader();
        p.setDocumentTypeIdentifier(new PeppolDocumentTypeId(namespace, localname, new CustomizationIdentifier(customization), version));
        p.setSenderId(new ParticipantId(sender));
        p.setRecipientId(new ParticipantId(receiver));
        p.setProfileTypeIdentifier(PeppolProcessTypeId.valueOf(profileId));
        return p;
    }

}
