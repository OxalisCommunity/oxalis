package eu.peppol.outbound.api;

import eu.peppol.outbound.smp.SmpLookupManager;
import eu.peppol.outbound.soap.SoapDispatcher;
import eu.peppol.outbound.soap.SoapHeader;
import eu.peppol.outbound.util.Identifiers;
import eu.peppol.start.util.Configuration;
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
 * The Oxalis START outbound module contains all necessary code for sending PEPPOL business documents via the START
 * protocol to a receiving Access Point.
 * <p/>
 * A DocumentSender is the publicly available interface class for sending documents. A particular DocumentSender
 * is dedicated to a particular document and process type. The class is thread-safe.
 * <p/>
 * User: nigel
 * Date: Oct 17, 2011
 * Time: 4:42:01 PM
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DocumentSender {

    private final DocumentIdentifierType documentId;
    private final ProcessIdentifierType processId;
    private final boolean soapLogging;

    DocumentSender(DocumentIdentifierType documentId, ProcessIdentifierType processId, boolean soapLogging) {
        this.documentId = documentId;
        this.processId = processId;
        this.soapLogging = soapLogging;
    }

    /**
     * sends a PEPPOL business document to a named recipient. The Access Point of the recipient will be identified
     * by SMP lookup.
     *
     * @param xmlDocument   the PEPPOL business document to be sent
     * @param recipient     the participant id of the document receiver
     */
    public void sendInvoice(InputStream xmlDocument, String recipient) throws Exception {
        sendInvoice(xmlDocument, recipient, getEndpointAddress(recipient));
    }

    /**
     * sends a PEPPOL business document to a named recipient. The Access Point of the recipient will be identified
     * by SMP lookup.
     *
     * @param xmlDocument   the PEPPOL business document to be sent
     * @param recipient     the participant id of the document receiver
     */
    public void sendInvoice(File xmlDocument, String recipient) throws Exception {
        sendInvoice(xmlDocument, recipient, getEndpointAddress(recipient));
    }

    /**
     * sends a PEPPOL business document to a named recipient. The destination parameter specifies the address of the
     * recipients Access Point. No SMP lookup will be involved.
     *
     * @param xmlDocument   the PEPPOL business document to be sent
     * @param recipient     the participant id of the document receiver
     * @param destination   the address of the recipient's access point
     */
    public void sendInvoice(InputStream xmlDocument, String recipient, URL destination) throws Exception {
        send(getDocumentBuilder().parse(xmlDocument), recipient, destination);
    }

    /**
     * sends a PEPPOL business document to a named recipient. The destination parameter specifies the address of the
     * recipients Access Point. No SMP lookup will be involved.
     *
     * @param xmlDocument   the PEPPOL business document to be sent
     * @param recipient     the participant id of the document receiver
     * @param destination   the address of the recipient's access point
     */
    public void sendInvoice(File xmlDocument, String recipient, URL destination) throws Exception {
        send(getDocumentBuilder().parse(xmlDocument), recipient, destination);
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

    private void send(Document document, String recipient, URL destination) {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        String sender = Configuration.getInstance().getProperty("own.participant.id");
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
