package eu.peppol.document;

import org.testng.annotations.Test;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.DocumentIdentification;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.StandardBusinessDocument;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author steinar
 *         Date: 23.10.13
 *         Time: 14:51
 */
public class ParseSbdhTest {

    @Test(enabled = false)
    public void testParseSbdh() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance("org.unece.cefact.namespaces.standardbusinessdocumentheader");

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        URL resource = ParseSbdhTest.class.getClassLoader().getResource("peppol-bis-invoice-sbdh.xml");
        assertNotNull(resource);

        File file = new File(resource.toURI());
        assertTrue(file.isFile() && file.canRead());

        FileInputStream fileInputStream = new FileInputStream(file);

        // Parses the entire XML document
        JAXBElement<StandardBusinessDocument> sbdh = (JAXBElement) unmarshaller.unmarshal(fileInputStream);
        assertNotNull(sbdh);

        StandardBusinessDocument standardBusinessDocument = sbdh.getValue();
        DocumentIdentification documentIdentification = standardBusinessDocument.getStandardBusinessDocumentHeader().getDocumentIdentification();
        assertNotNull(documentIdentification);

        String type = standardBusinessDocument.getStandardBusinessDocumentHeader().getBusinessScope().getScope().get(0).getType();
        String instanceIdentifier = standardBusinessDocument.getStandardBusinessDocumentHeader().getBusinessScope().getScope().get(0).getInstanceIdentifier();

        System.out.println(type);
        System.out.println(instanceIdentifier);

        // Grabs the payload (the Invoice), which is simply declared as type "xs:any"
        /*
        Object any = standardBusinessDocument.getAny();
        Element element = (Element) any;

        // Serializes the XML payload inside the SBDH
        Document document = element.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();

        LSSerializer serializer = domImplLS.createLSSerializer();

        // create an outputter which we can tell to use UTF-8 as encoding
        LSOutput lsOutput = domImplLS.createLSOutput();
        lsOutput.setEncoding("UTF-8");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        lsOutput.setByteStream(stream);

        serializer.write(document, lsOutput);
        assertTrue(stream.toString().contains("AccountingSupplierParty"));
        */

    }


}
