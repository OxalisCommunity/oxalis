package eu.peppol.outbound.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import eu.peppol.outbound.transmission.TransmissionTestModule;

import java.io.InputStream;

import static org.testng.Assert.assertNotNull;

/**
 * @author steinar
 *         Date: 04.11.13
 *         Time: 10:15
 */
public class TestResourceModule extends AbstractModule {

    public static final String PEPPOL_BIS_INVOICE_SBD_XML = "peppol-bis-invoice-sbdh.xml";
    public static final String EHF_T10_ALLE_ELEMENTER_XML = "ehf-t10-alle-elementer.xml";


    @Override
    protected void configure() {
    }


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
        assertNotNull(inputStream, "Unable to laod " + EHF_T10_ALLE_ELEMENTER_XML + " from class path");

        return inputStream;
    }
}
