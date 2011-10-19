package eu.peppol.outbound.client;

import eu.peppol.outbound.soap.SOAPHeaderObject;
import eu.peppol.outbound.util.Constants;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * User: nigel
 * Date: Oct 17, 2011
 * Time: 4:42:01 PM
 */
public class PeppolDocumentSender {

    public void sendInvoiceWithSmpLookup(File xmlDocument, ParticipantIdentifierType recipient) throws Exception {
        sendInvoice(xmlDocument, recipient, new URL(""));
    }

    public void sendInvoice(File xmlDocument, ParticipantIdentifierType recipient, URL destination) throws Exception {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = parser.parse(xmlDocument);
        Create create = new Create();
        List<Object> objects = create.getAny();
        objects.add(document.getDocumentElement());

        SOAPHeaderObject soapHeaderObject = new SOAPHeaderObject();
        soapHeaderObject.setChannelIdentifier("");
        soapHeaderObject.setMessageIdentifier("uuid:" + UUID.randomUUID().toString());
        soapHeaderObject.setDocumentIdentifier(Constants.getInvoiceDocumentIdentifier());
        soapHeaderObject.setProcessIdentifier(Constants.getInvoiceProcessIdentifier());
        soapHeaderObject.setSenderIdentifier(Constants.getOwnParticipantIdentifier());
        soapHeaderObject.setRecipientIdentifier(recipient);

        accesspointClient accesspointClient = new accesspointClient();
        accesspointClient.enableSoapLogging(false);
        accesspointClient.send(destination.toExternalForm(), soapHeaderObject, create);
    }

}
