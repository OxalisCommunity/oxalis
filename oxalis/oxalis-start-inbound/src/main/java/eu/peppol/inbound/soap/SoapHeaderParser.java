package eu.peppol.inbound.soap;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import eu.peppol.outbound.soap.SoapHeader;
import eu.peppol.start.util.IdentifierName;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

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
        Header messageIdentifierHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, IdentifierName.MESSAGE_ID.getValue()), false);
        String messageIdentifier = messageIdentifierHeader.getStringContent();
        return messageIdentifier;

    }

    private static String channelIdentifier(HeaderList hl) {
        Header channelIdentifierHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, IdentifierName.CHANNEL_ID.getValue()), false);

        return channelIdentifierHeader.getStringContent();
    }


    static ParticipantIdentifierType recipientIdentifier(HeaderList hl) {
        return participantIdentifer(hl, IdentifierName.RECIPIENT_ID.getValue());
    }

    static ParticipantIdentifierType senderIdentifier(HeaderList hl) {
        return participantIdentifer(hl, IdentifierName.SENDER_ID.getValue());
    }

    /** Convenience method for extracting the sender or the receiver participant identifier */
    static ParticipantIdentifierType participantIdentifer(HeaderList hl, String headerName) {
        Header participantIdentiferHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, headerName), false);

        // Creates our type
        ParticipantIdentifierType participantIdentifierType = new ParticipantIdentifierType();
        participantIdentifierType.setScheme(participantIdentiferHeader.getAttribute(new QName(null, IdentifierName.SCHEME.getValue()))); // The scheme
        participantIdentifierType.setValue(participantIdentiferHeader.getStringContent());     // The actual value
        return participantIdentifierType;

    }

    static DocumentIdentifierType documentIdentifierType(HeaderList hl) {
        Header documentIdentifierHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, IdentifierName.DOCUMENT_ID.getValue()), false);
        DocumentIdentifierType documentIdentifierType = new DocumentIdentifierType();
        documentIdentifierType.setValue(documentIdentifierHeader.getStringContent());

        String scheme = documentIdentifierHeader.getAttribute(new QName(null, IdentifierName.SCHEME.getValue()));
        documentIdentifierType.setScheme(scheme); // The scheme

        return documentIdentifierType;
    }

    static ProcessIdentifierType processIdentifier(HeaderList hl) {
        Header processIdentifierHeader = hl.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, IdentifierName.PROCESS_ID.getValue()), false);
        ProcessIdentifierType processIdentifierType = new ProcessIdentifierType();
        processIdentifierType.setValue(processIdentifierHeader.getStringContent());

        String scheme = processIdentifierHeader.getAttribute(new QName(null, IdentifierName.SCHEME.getValue()));
        processIdentifierType.setScheme(scheme);
        return processIdentifierType;
    }


}
