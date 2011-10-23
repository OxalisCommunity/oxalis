package eu.peppol.outbound.client;

import eu.peppol.outbound.soap.SOAPHeaderObject;
import eu.peppol.outbound.util.Identifiers;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;
import java.util.UUID;

/**
 * User: nigel
 * Date: Oct 17, 2011
 * Time: 4:42:01 PM
 */
public class PeppolDocumentSender {

    public void sendInvoiceWithSmpLookup(File xmlDocument, String recipient) throws Exception {
        sendInvoice(xmlDocument, recipient, new URL(""));
    }

    public void sendInvoice(File xmlDocument, String recipient, URL destination) throws Exception {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        if (!Identifiers.isValidParticipantIdentifier(recipient)) {
            throw new IllegalArgumentException("Invalid participantId " + recipient);
        }

        ParticipantIdentifierType recipientId = Identifiers.getParticipantIdentifier(recipient);
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Create create = new Create();
        create.getAny().add(documentBuilder.parse(xmlDocument).getDocumentElement());

        SOAPHeaderObject soapHeaderObject = new SOAPHeaderObject();
        soapHeaderObject.setChannelIdentifier("");
        soapHeaderObject.setMessageIdentifier("uuid:" + UUID.randomUUID().toString());
        soapHeaderObject.setDocumentIdentifier(Identifiers.getInvoiceDocumentIdentifier());
        soapHeaderObject.setProcessIdentifier(Identifiers.getInvoiceProcessIdentifier());
        soapHeaderObject.setSenderIdentifier(Identifiers.getOwnParticipantIdentifier());
        soapHeaderObject.setRecipientIdentifier(recipientId);

        accesspointClient accesspointClient = new accesspointClient();
        accesspointClient.enableSoapLogging(false);
        accesspointClient.send(destination.toExternalForm(), soapHeaderObject, create);
    }
}
