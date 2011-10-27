package eu.peppol.outbound.api;

import eu.peppol.outbound.smp.SmpLookupManager;
import eu.peppol.outbound.soap.SoapDispatcher;
import eu.peppol.outbound.soap.SoapHeader;
import eu.peppol.outbound.util.Identifiers;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;
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
@SuppressWarnings({"UnusedDeclaration"})
public class DocumentSender {

    private DocumentIdentifierType documentId;
    private ProcessIdentifierType processId;
    private boolean soapLogging;

    DocumentSender(DocumentIdentifierType documentId, ProcessIdentifierType processId, boolean soapLogging) {
        this.documentId = documentId;
        this.processId = processId;
        this.soapLogging = soapLogging;
    }

    public void sendInvoice(InputStream xmlDocument, String sender, String recipient) throws Exception {
        sendInvoice(xmlDocument, sender, recipient, getEndpointAddress(recipient));
    }

    public void sendInvoice(File xmlDocument, String sender, String recipient) throws Exception {
        sendInvoice(xmlDocument, sender, recipient, getEndpointAddress(recipient));
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

    private URL getEndpointAddress(String recipient) {
        return new SmpLookupManager().getEndpointAddress(getParticipantId(recipient), documentId);
    }

    private ParticipantIdentifierType getParticipantId(String sender) {
        if (!Identifiers.isValidParticipantIdentifier(sender)) {
            throw new IllegalArgumentException("Invalid participant " + sender);
        }

        return Identifiers.getParticipantIdentifier(sender);
    }

    private void send(Document document, String sender, String recipient, URL destination) {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        ParticipantIdentifierType senderId = getParticipantId(sender);
        ParticipantIdentifierType recipientId = getParticipantId(recipient);
        Create create = new Create();
        create.getAny().add(document.getDocumentElement());

        SoapHeader soapHeader = new SoapHeader();
        soapHeader.setChannelIdentifier("");
        soapHeader.setMessageIdentifier("uuid:" + UUID.randomUUID().toString());
        soapHeader.setDocumentIdentifier(documentId);
        soapHeader.setProcessIdentifier(processId);
        soapHeader.setSenderIdentifier(senderId);
        soapHeader.setRecipientIdentifier(recipientId);

        SoapDispatcher soapDispatcher = new SoapDispatcher();
        soapDispatcher.enableSoapLogging(soapLogging);
        soapDispatcher.send(destination, soapHeader, create);
    }
}
