package eu.peppol.outbound.transmission;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.*;
import eu.peppol.outbound.soap.SoapDispatcher;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.identifier.StartMessageHeader;
import eu.peppol.util.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.FaultMessage;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.UUID;

/**
 * @author steinar
 *         Date: 05.11.13
 *         Time: 13:16
 */
class StartMessageSender implements MessageSender {

    public static final Logger log = LoggerFactory.getLogger(StartMessageSender.class);
    private final SoapDispatcher soapDispatcher;
    private final GlobalConfiguration globalConfiguration;

    @Inject
    StartMessageSender(SoapDispatcher soapDispatcher, GlobalConfiguration globalConfiguration) {
        this.soapDispatcher = soapDispatcher;
        this.globalConfiguration = globalConfiguration;
    }

    @Override
    public TransmissionResponse send(TransmissionRequest transmissionRequest) {

        Document document = parsePayload(transmissionRequest);
        PeppolStandardBusinessHeader sbdh = transmissionRequest.getPeppolStandardBusinessHeader();

        try {
            MessageId messageId = send(document,
                    sbdh.getDocumentTypeIdentifier(),
                    sbdh.getProfileTypeIdentifier(),
                    sbdh.getSenderId(),
                    sbdh.getRecipientId(),
                    transmissionRequest.getEndpointAddress().getUrl());
            // for START the transmissionId of a successful transfer will be the same as the messageId UUID
            TransmissionId transmissionId = new TransmissionId(messageId.toUUID());
            StartTransmissionResponse startTransmissionResponse = new StartTransmissionResponse(transmissionId, sbdh, transmissionRequest.getEndpointAddress().getUrl(), BusDoxProtocol.START);
            return startTransmissionResponse;

        } catch (FaultMessage faultMessage) {
            throw new IllegalStateException("Unable to send message: " + faultMessage.getMessage(), faultMessage);
        }
    }

    Document parsePayload(TransmissionRequest transmissionRequest) {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(transmissionRequest.getPayload());

        try {
            log.debug("Constructing document body....");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            // Prevents XML entity expansion attacks
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Document document = documentBuilderFactory.newDocumentBuilder().parse(byteArrayInputStream);
            return document;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parseMultipart byte stream into a valid XML Document; " + e.getMessage(), e);
        }
    }


    MessageId send(Document document, PeppolDocumentTypeId documentTypeIdentifier,
                           PeppolProcessTypeId peppolProcessTypeId,
                           ParticipantId senderId, ParticipantId recipientId,
                           URL destination) throws FaultMessage {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        Create soapBody = new Create();
        soapBody.getAny().add(document.getDocumentElement());

        Log.debug("Constructing SOAP header");
        StartMessageHeader messageHeader= new StartMessageHeader();

        MessageId messageId = new MessageId("uuid:" + UUID.randomUUID().toString());

        messageHeader.setMessageId(messageId);
        messageHeader.setDocumentTypeIdentifier(documentTypeIdentifier);
        messageHeader.setPeppolProcessTypeId(peppolProcessTypeId);
        messageHeader.setSenderId(senderId);
        messageHeader.setRecipientId(recipientId);

        soapDispatcher.enableSoapLogging(globalConfiguration.isSoapTraceEnabled());

        soapDispatcher.send(destination, messageHeader, soapBody);

        return messageId;
    }

}
