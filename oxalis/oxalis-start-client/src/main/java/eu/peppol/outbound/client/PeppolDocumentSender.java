package eu.peppol.outbound.client;

import eu.peppol.outbound.soap.SOAPHeaderObject;
import eu.peppol.outbound.util.Identifiers;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

/**
 * User: nigel
 * Date: Oct 17, 2011
 * Time: 4:42:01 PM
 */
public class PeppolDocumentSender {

    PeppolDocumentSender() {
    }

    public void sendInvoiceWithSmpLookup(File xmlDocument, String sender, String recipient) throws Exception {
        sendInvoice(xmlDocument, sender, recipient, new URL(""));
    }

    public void sendInvoice(InputStream xmlDocument, String sender, String recipient, URL destination) throws Exception {
        send(getDocumentBuilder().parse(xmlDocument), sender, recipient, destination);
    }

    public void sendInvoice(File xmlDocument, String sender, String recipient, URL destination) throws Exception {
        send(getDocumentBuilder().parse(xmlDocument), sender, recipient, destination);
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    private void send(Document document, String sender, String recipient, URL destination) {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        if (!Identifiers.isValidParticipantIdentifier(sender)) {
            throw new IllegalArgumentException("Invalid sender " + sender);
        }

        if (!Identifiers.isValidParticipantIdentifier(recipient)) {
            throw new IllegalArgumentException("Invalid recipient " + recipient);
        }

        ParticipantIdentifierType senderId = Identifiers.getParticipantIdentifier(sender);
        ParticipantIdentifierType recipientId = Identifiers.getParticipantIdentifier(recipient);
        Create create = new Create();
        create.getAny().add(document.getDocumentElement());

        SOAPHeaderObject soapHeaderObject = new SOAPHeaderObject();
        soapHeaderObject.setChannelIdentifier("");
        soapHeaderObject.setMessageIdentifier("uuid:" + UUID.randomUUID().toString());
        soapHeaderObject.setDocumentIdentifier(Identifiers.getInvoiceDocumentIdentifier());
        soapHeaderObject.setProcessIdentifier(Identifiers.getInvoiceProcessIdentifier());
        soapHeaderObject.setSenderIdentifier(senderId);
        soapHeaderObject.setRecipientIdentifier(recipientId);

        accesspointClient accesspointClient = new accesspointClient();
        accesspointClient.enableSoapLogging(false);
        accesspointClient.send(destination.toExternalForm(), soapHeaderObject, create);
    }
}
