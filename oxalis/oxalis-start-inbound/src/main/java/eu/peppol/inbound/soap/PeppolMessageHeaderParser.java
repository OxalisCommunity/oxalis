package eu.peppol.inbound.soap;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import eu.peppol.start.identifier.*;

import javax.xml.namespace.QName;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 19:47
 */
public class PeppolMessageHeaderParser {

    public static PeppolMessageHeader parseSoapHeaders(HeaderList headerList) {
        PeppolMessageHeader m = new PeppolMessageHeader();

        m.setMessageId(messageIdentifier(headerList));
        m.setChannelId(channelIdentifier(headerList));
        m.setRecipientId(recipientIdentifier(headerList));
        m.setSenderId(senderIdentifier(headerList));
        m.setDocumentId(documentIdentifer(headerList));
        m.setProcessId(processIdentifier(headerList));
        
        return m;
    }



    static MessageId messageIdentifier(HeaderList headerList) {
        Header messageIdentifierHeader = headerList.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, IdentifierName.MESSAGE_ID.stringValue()), false);
        String messageIdentifier = messageIdentifierHeader.getStringContent();
        return new MessageId(messageIdentifier);
    }

    static ChannelId channelIdentifier(HeaderList headerList) {
        Header channelIdentifierHeader = headerList.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, IdentifierName.CHANNEL_ID.stringValue()), false);

        return new ChannelId(channelIdentifierHeader.getStringContent());
    }

    static ParticipantId recipientIdentifier(HeaderList headerList) {
        return participantId(headerList, IdentifierName.RECIPIENT_ID.stringValue());
    }

    static ParticipantId participantId(HeaderList headerList, String headerName) {
        Header participantIdentiferHeader = headerList.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, headerName), false);
        return new ParticipantId(participantIdentiferHeader.getStringContent());
    }


    static ParticipantId senderIdentifier(HeaderList headerList) {
        return participantId(headerList, IdentifierName.SENDER_ID.stringValue());
    }


    static DocumentId documentIdentifer(HeaderList headerList) {
        Header documentIdentifierHeader = headerList.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, IdentifierName.DOCUMENT_ID.stringValue()), false);
        DocumentId documentId = DocumentId.valueFor(documentIdentifierHeader.getStringContent());
        return documentId;
    }


    private static ProcessId processIdentifier(HeaderList headerList) {
        Header processIdentifierHeader = headerList.get(new QName(SOAPHeaderDocument.NAMESPACE_TRANSPORT_IDS, IdentifierName.PROCESS_ID.stringValue()), false);
        ProcessId processId = ProcessId.valueFor(processIdentifierHeader.getStringContent());
        return processId;
    }
}
