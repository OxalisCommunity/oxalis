package eu.peppol.inbound.soap;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.developer.JAXWSProperties;
import eu.peppol.inbound.soap.handler.SOAPInboundHandler;
import eu.peppol.inbound.util.Log;
import eu.peppol.outbound.soap.SoapHeader;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

import javax.swing.text.Document;
import javax.xml.namespace.QName;

/**
 * Extracts the various PEPPOL headers from a SOAP HeaderList
 *
 * @author Steinar Overbeck Cook
 *         Created by
 *         User: steinar
 *         Date: 28.11.11
 *         Time: 09:19
 */
public class SoapHeaderParser {

    public static SoapHeader fetchPeppolSoapHeader(HeaderList hl) {

        SoapHeader soapHeader = new SoapHeader();

        soapHeader.setMessageIdentifier(messageIdentifier(hl));
        soapHeader.setChannelIdentifier(channelIdentifier(hl));
        soapHeader.setRecipientIdentifier(recipientIdentifier(hl));
        soapHeader.setSenderIdentifier(senderIdentifier(hl));
        soapHeader.setDocumentIdentifier(documentIdentifierType(hl));
        soapHeader.setProcessIdentifier(processIdentifier(hl));

        return soapHeader;
    }

    private static String messageIdentifier(HeaderList hl) {
        Header messageIdentifierHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, SOAPInboundHandler.MESSAGE_ID), false);
        String messageIdentifier = messageIdentifierHeader.getStringContent();
        return messageIdentifier;

    }

    private static String channelIdentifier(HeaderList hl) {
        Header channelIdentifierHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, SOAPInboundHandler.CHANNEL_ID), false);

        return channelIdentifierHeader.getStringContent();
    }


    static ParticipantIdentifierType recipientIdentifier(HeaderList hl) {
        return participantIdentifer(hl, SOAPInboundHandler.RECIPIENT_ID);
    }

    static ParticipantIdentifierType senderIdentifier(HeaderList hl) {
        return participantIdentifer(hl, SOAPInboundHandler.SENDER_ID);
    }

    /** Convenience method for extracting the sender or the receiver participant identifier */
    static ParticipantIdentifierType participantIdentifer(HeaderList hl, String headerName) {
        Header participantIdentiferHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, headerName), false);

        // Creates our type
        ParticipantIdentifierType participantIdentifierType = new ParticipantIdentifierType();
        participantIdentifierType.setScheme(participantIdentiferHeader.getAttribute(new QName(null, SOAPInboundHandler.SCHEME))); // The scheme
        participantIdentifierType.setValue(participantIdentiferHeader.getStringContent());     // The actual value
        return participantIdentifierType;

    }

    static DocumentIdentifierType documentIdentifierType(HeaderList hl) {
        Header documentIdentifierHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, SOAPInboundHandler.DOCUMENT_ID), false);
        DocumentIdentifierType documentIdentifierType = new DocumentIdentifierType();
        documentIdentifierType.setValue(documentIdentifierHeader.getStringContent());

        String scheme = documentIdentifierHeader.getAttribute(new QName(null, SOAPInboundHandler.SCHEME));
        documentIdentifierType.setScheme(scheme); // The scheme

        return documentIdentifierType;
    }

    static ProcessIdentifierType processIdentifier(HeaderList hl) {
        Header processIdentifierHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, SOAPInboundHandler.PROCESS_ID), false);
        ProcessIdentifierType processIdentifierType = new ProcessIdentifierType();
        processIdentifierType.setValue(processIdentifierHeader.getStringContent());

        String scheme = processIdentifierHeader.getAttribute(new QName(null, SOAPInboundHandler.SCHEME));
        processIdentifierType.setScheme(scheme);
        return processIdentifierType;
    }


}
