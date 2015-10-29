package no.difi.oxalis.peppol.service;

import eu.peppol.identifier.Endpoint;
import eu.peppol.sbdh.SbdhMessageHeader;
import no.difi.oxalis.smp.service.SmpLookupService;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocumentHeader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.net.URI;

public class SmpLookupServiceTest {

    private JAXBContext jaxbContext;
    private SmpLookupService smpLookupService;

    @BeforeClass
    public void beforeClass() throws Exception {
        jaxbContext = JAXBContext.newInstance(StandardBusinessDocumentHeader.class);
        smpLookupService = new SmpLookupService();
    }

    @Test(enabled = false)
    public void validInvoice() throws Exception {
        StandardBusinessDocumentHeader header = loadResourceHeader("/sbdh/header-invoice.xml");
        Endpoint endpoint = smpLookupService.getEndpoint(new SbdhMessageHeader(header), "busdox-transport-as2-ver1p0");

        Assert.assertEquals(endpoint.getAddress(), URI.create("https://aksesspunkt.difi.no/as2"));
    }

    private StandardBusinessDocumentHeader loadResourceHeader(String filename) throws Exception {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(getClass().getResourceAsStream("/sbdh/header-invoice.xml")),
                StandardBusinessDocumentHeader.class
        ).getValue();
    }

}
