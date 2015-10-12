package eu.peppol.inbound.util;

import org.testng.annotations.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: steinar
 * Date: 27.09.12
 * Time: 21:07
 */
public class ParseAndSaveEHFTest {


    @Test
    public void parseAndSave() throws ParserConfigurationException, URISyntaxException, IOException, SAXException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        URL invoiceUrl = ParseAndSaveEHFTest.class.getClassLoader().getResource("BII04 T10 gyldig faktura med alle elementer.xml");
        File file = new File(invoiceUrl.toURI());

        Document document =  documentBuilder.parse(file);

        Element documentElement = document.getDocumentElement();
        NamedNodeMap namedNodeMap = documentElement.getAttributes();
        for (int i = 0; i < namedNodeMap.getLength(); i++) {
            Attr attr = (Attr) namedNodeMap.item(i);
            System.err.format("%s %s = %s\n", attr.getLocalName(), attr.getName(),attr.getValue());
        }


        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.transform(new DOMSource(document), result);

//            System.out.println(writer.toString());
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Unable to create an XML transformer");
        } catch (TransformerException e) {
            throw new IllegalStateException("Unable to transform XML document into a string");
        }




    }
}
