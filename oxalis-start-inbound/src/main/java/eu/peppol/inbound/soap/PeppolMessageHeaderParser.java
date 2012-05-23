package eu.peppol.inbound.soap;

import com.sun.xml.ws.api.message.HeaderList;
import eu.peppol.start.identifier.*;

import javax.xml.namespace.QName;

import static eu.peppol.start.identifier.IdentifierName.*;

/**
 * Parses the PEPPOL SOAP Headers into a simple structure, which contains the meta data for the
 * message being transferred.
 * 
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 04.12.11
 *         Time: 19:47
 */
public class PeppolMessageHeaderParser {

    private static final String NAMESPACE_TRANSPORT_IDS = "http://busdox.org/transport/identifiers/1.0/";

    public static PeppolMessageHeader parseSoapHeaders(HeaderList headerList) {
        PeppolMessageHeader m = new PeppolMessageHeader();

        m.setMessageId(new MessageId(getContent(headerList, MESSAGE_ID)));
        m.setChannelId(new ChannelId(getContent(headerList, CHANNEL_ID)));
        m.setRecipientId(new ParticipantId(getContent(headerList, RECIPIENT_ID.stringValue())));
        m.setSenderId(new ParticipantId(getContent(headerList, SENDER_ID.stringValue())));
        m.setDocumentTypeIdentifier(PeppolDocumentTypeId.valueOf(getContent(headerList, DOCUMENT_ID)));
        m.setPeppolProcessTypeId(PeppolProcessTypeId.valueOf(getContent(headerList, PROCESS_ID)));

        return m;
    }

    private static QName getQName(IdentifierName identifierName) {
        return getQName(identifierName.stringValue());
    }

    private static QName getQName(String headerName) {
        return new QName(NAMESPACE_TRANSPORT_IDS, headerName);
    }

    private static String getContent(HeaderList headerList, IdentifierName identifierName) {
        return headerList.get(getQName(identifierName), false).getStringContent();
    }

    private static String getContent(HeaderList headerList, String identifierName) {
        return headerList.get(getQName(identifierName), false).getStringContent();
    }
}
