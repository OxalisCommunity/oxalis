package eu.peppol.outbound.api;

import eu.peppol.identifier.*;
import eu.peppol.outbound.OxalisOutboundModule;
import eu.peppol.outbound.soap.SoapDispatcher;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.start.identifier.StartMessageHeader;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.RawStatisticsRepository;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.FaultMessage;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
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
 * There are 2 main variants of the sendInvoice method. The first variant uses SMP to find the destination AP. If
 * the SMP lookup fails then the document will not be sent. The second variant sends a document to a specified AP. In
 * this case eu SMP lookup is involved.
 * <p/>
 * User: nigel
 * Date: Oct 17, 2011
 * Time: 4:42:01 PM
 *
 * @deprecated since version 3.x, please use the Transmitter together with TransmissionRequestBuilder
 *
 * @see eu.peppol.outbound.transmission.TransmissionRequestBuilder
 * @see eu.peppol.outbound.transmission.TransmissionRequest
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DocumentSender {

    private final PeppolDocumentTypeId documentTypeIdentifier;
    private final PeppolProcessTypeId peppolProcessTypeId;
    private final boolean soapLogging;
    private final RawStatisticsRepository rawStatisticsRepository;
    private final AccessPointIdentifier accessPointIdentifier;
    private SoapDispatcher soapDispatcher;

    DocumentSender(PeppolDocumentTypeId documentTypeIdentifier, PeppolProcessTypeId processId, boolean soapLogging, RawStatisticsRepository rawStatisticsRepository, AccessPointIdentifier accessPointIdentifier) {
        this.documentTypeIdentifier = documentTypeIdentifier;
        this.peppolProcessTypeId = processId;
        this.soapLogging = soapLogging;
        this.rawStatisticsRepository = rawStatisticsRepository;
        this.accessPointIdentifier = accessPointIdentifier;

        this.soapDispatcher = new SoapDispatcher();
    }

    public void sendMessage(InputStream inputStream, String s, String s1) {
    }

    /**
     * sends a PEPPOL business document to a named recipient. The Access Point of the recipient will be identified
     * by SMP lookup.
     *
     * @param xmlDocument the PEPPOL business document to be sent
     * @param sender      the participant id of the document sender
     * @param recipient   the participant id of the document receiver
     * @param channelId     holds the PEPPOL ChannelID to be used
     * @return message id assigned
     */
    public MessageId sendInvoice(InputStream xmlDocument, String sender, String recipient, String  channelId) throws FaultMessage {
        URL endpointAddress = getEndpointAddress(recipient);
        return sendInvoice(xmlDocument, sender, recipient, endpointAddress, channelId);
    }

    /**
     * sends a PEPPOL business document to a named recipient. The Access Point of the recipient will be identified
     * by SMP lookup.
     *
     * @param xmlDocument the PEPPOL business document to be sent
     * @param sender      the participant id of the document sender
     * @param recipient   the participant id of the document receiver
     * @param channelId     holds the PEPPOL ChannelID to be used
     * @return message id assigned
     */
    public MessageId sendInvoice(File xmlDocument, String sender, String recipient, String  channelId) throws FaultMessage {
        URL endpointAddress = getEndpointAddress(recipient);
        return sendInvoice(xmlDocument, sender, recipient, endpointAddress, channelId);
    }

    /**
     * sends a PEPPOL business document to a named recipient. The destination parameter specifies the address of the
     * recipients Access Point. No SMP lookup will be involved.
     *
     * @param xmlDocument the PEPPOL business document to be sent
     * @param sender      the participant id of the document sender
     * @param recipient   the participant id of the document receiver
     * @param destination the address of the recipient's access point
     * @param channelId     holds the PEPPOL ChannelID to be used
     * @return message id assigned
     */
    public MessageId sendInvoice(InputStream xmlDocument, String sender, String recipient, URL destination, String  channelId) throws FaultMessage {
        log(destination);
        Document document;
        try {
            document = getDocumentBuilder().parse(xmlDocument);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parseMultipart xml document from " + sender + " to " + recipient + "; " + e, e);
        }
        return send(document, sender, recipient, destination, new ChannelId(channelId));
    }

    /**
     * sends a PEPPOL business document to a named recipient. The destination parameter specifies the address of the
     * recipients Access Point. No SMP lookup will be involved.
     *
     * @param xmlDocument the PEPPOL business document to be sent
     * @param sender      the participant id of the document sender
     * @param recipient   the participant id of the document receiver
     * @param destination the address of the recipient's access point
     * @param channelId     holds the PEPPOL ChannelID to be used
     * @return message id (UUID) assigned
     */
    public MessageId sendInvoice(File xmlDocument, String sender, String recipient, URL destination, String channelId) throws FaultMessage {
        log(destination);
        Document document;
        try {
            document = getDocumentBuilder().parse(xmlDocument);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parseMultipart XML Document in file " + xmlDocument + "; " + e, e);
        }
        return send(document, sender, recipient, destination, new ChannelId(channelId));
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // Prevents XML entity expansion attacks
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);

        return documentBuilderFactory.newDocumentBuilder();
    }

    private URL getEndpointAddress(String recipient) {
        return new OxalisOutboundModule().getSmpLookupManager().getEndpointAddress(getParticipantId(recipient), documentTypeIdentifier);
    }

    private ParticipantId getParticipantId(String sender) {
        if (!ParticipantId.isValidParticipantIdentifier(sender)) {
            throw new IllegalArgumentException("Invalid participant " + sender);
        }

        return new ParticipantId(sender);
    }

    private void log(URL destination) {
        Log.info("Document destination is " + destination);
    }


    private MessageId send(Document document, String sender, String recipient, URL destination, ChannelId channelId) throws FaultMessage {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        Log.debug("Constructing document body");
        ParticipantId senderId = getParticipantId(sender);
        ParticipantId recipientId = getParticipantId(recipient);

        Create soapBody = new Create();
        soapBody.getAny().add(document.getDocumentElement());

        Log.debug("Constructing SOAP header");
        StartMessageHeader messageHeader= new StartMessageHeader();
        messageHeader.setChannelId(channelId);

        MessageId messageId = new MessageId("uuid:" + UUID.randomUUID().toString());
        messageHeader.setMessageId(messageId);
        messageHeader.setDocumentTypeIdentifier(documentTypeIdentifier);
        messageHeader.setPeppolProcessTypeId(peppolProcessTypeId);
        messageHeader.setSenderId(senderId);
        messageHeader.setRecipientId(recipientId);

        soapDispatcher.enableSoapLogging(soapLogging);

        soapDispatcher.send(destination, messageHeader, soapBody);

        persistStatistics(messageHeader);

        return messageId;
    }

    void persistStatistics(StartMessageHeader messageHeader) {

        RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder()
                .accessPointIdentifier(accessPointIdentifier)   // Identifier predefined in Oxalis global config file
                .outbound()
                .documentType(messageHeader.getDocumentTypeIdentifier())
                .sender(messageHeader.getSenderId())
                .receiver(messageHeader.getRecipientId())
                .profile(messageHeader.getPeppolProcessTypeId())
                .channel(messageHeader.getChannelId())
                .build();
        rawStatisticsRepository.persist(rawStatistics);
    }
}
