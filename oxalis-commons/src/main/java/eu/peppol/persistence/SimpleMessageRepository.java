package eu.peppol.persistence;

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Default implementation of MessageRepository supplied as part of the Oxalis distribution.
 * Received messages are stored in the file system using JSON and XML.  Configure directory
 * to store messages in oxalis-global.properties as property "oxalis.inbound.message.store".
 *
 * @author Steinar
 * @author Thore
 */
public class SimpleMessageRepository implements MessageRepository {

    private static final Logger log = LoggerFactory.getLogger(SimpleMessageRepository.class);
    private final GlobalConfiguration globalConfiguration;

    public SimpleMessageRepository(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    @Override
    public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, Document document) throws OxalisMessagePersistenceException {

        log.info("Saving inbound message document using " + SimpleMessageRepository.class.getSimpleName());
        log.debug("Default inbound message headers " + peppolMessageMetaData);

        File messageDirectory = prepareMessageDirectory(globalConfiguration.getInboundMessageStore(), peppolMessageMetaData.getRecipientId(), peppolMessageMetaData.getSenderId());

        try {

            File messageFullPath = computeMessageFileName(peppolMessageMetaData.getTransmissionId(), messageDirectory);
            saveDocument(document, messageFullPath);

            File messageHeaderFilePath = computeHeaderFileName(peppolMessageMetaData.getTransmissionId(), messageDirectory);
            saveHeader(peppolMessageMetaData, messageHeaderFilePath);

        } catch (Exception e) {
            throw new OxalisMessagePersistenceException(peppolMessageMetaData, e);
        }

    }

    @Override
    public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream payloadInputStream) throws OxalisMessagePersistenceException {

        log.info("Saving inbound message stream using " + SimpleMessageRepository.class.getSimpleName());
        log.debug("Default inbound message headers " + peppolMessageMetaData);

        File messageDirectory = prepareMessageDirectory(globalConfiguration.getInboundMessageStore(), peppolMessageMetaData.getRecipientId(), peppolMessageMetaData.getSenderId());

        try {

            File messageFullPath = computeMessageFileName(peppolMessageMetaData.getTransmissionId(), messageDirectory);
            saveDocument(payloadInputStream, messageFullPath);

            File messageHeaderFilePath = computeHeaderFileName(peppolMessageMetaData.getTransmissionId(), messageDirectory);
            saveHeader(peppolMessageMetaData, messageHeaderFilePath);

        } catch (Exception e) {
            throw new OxalisMessagePersistenceException(peppolMessageMetaData, e);
        }

    }

    private File computeHeaderFileName(TransmissionId messageId, File messageDirectory) {
        String headerFileName = normalize(messageId.toString()) + ".txt";
        return new File(messageDirectory, headerFileName);
    }

    private File computeMessageFileName(TransmissionId messageId, File messageDirectory) {
        String messageFileName = normalize(messageId.toString()) + ".xml";
        return new File(messageDirectory, messageFileName);
    }

    File prepareMessageDirectory(String inboundMessageStore, ParticipantId recipient, ParticipantId sender) {
        // Computes the full path of the directory in which message and routing data should be stored.
        File messageDirectory = computeDirectoryNameForInboundMessage(inboundMessageStore, recipient, sender);
        if (!messageDirectory.exists()){
            if (!messageDirectory.mkdirs()){
                throw new IllegalStateException("Unable to create directory " + messageDirectory.toString());
            }
        }
        if (!messageDirectory.isDirectory() || !messageDirectory.canWrite()) {
            throw new IllegalStateException("Directory " + messageDirectory + " does not exist, or there is no access");
        }
        return messageDirectory;
    }

    /**
     * Transforms and saves the headers as JSON
     */
    void saveHeader(PeppolMessageMetaData peppolMessageMetaData, File messageHeaderFilePath) {
        try {
            FileOutputStream fos = new FileOutputStream(messageHeaderFilePath);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
            pw.write(getHeadersAsJSON(peppolMessageMetaData));
            pw.close();
            log.debug("File " + messageHeaderFilePath + " written");
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to create file " + messageHeaderFilePath + "; " + e, e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to create writer for " + messageHeaderFilePath + "; " + e, e);
        }
    }

    String getHeadersAsJSON(PeppolMessageMetaData headers) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{ \"PeppolMessageMetaData\" :\n  {\n");
            sb.append(createJsonPair("messageId", headers.getMessageId()));
            sb.append(createJsonPair("recipientId", headers.getRecipientId()));
            sb.append(createJsonPair("recipientSchemeId", getSchemeId(headers.getRecipientId())));
            sb.append(createJsonPair("senderId", headers.getSenderId()));
            sb.append(createJsonPair("senderSchemeId", getSchemeId(headers.getSenderId())));
            sb.append(createJsonPair("documentTypeIdentifier", headers.getDocumentTypeIdentifier()));
            sb.append(createJsonPair("profileTypeIdentifier", headers.getProfileTypeIdentifier()));
            sb.append(createJsonPair("sendingAccessPoint", headers.getSendingAccessPoint()));
            sb.append(createJsonPair("receivingAccessPoint", headers.getReceivingAccessPoint()));
            sb.append(createJsonPair("protocol", headers.getProtocol()));
            sb.append(createJsonPair("userAgent", headers.getUserAgent()));
            sb.append(createJsonPair("userAgentVersion", headers.getUserAgentVersion()));
            sb.append(createJsonPair("sendersTimeStamp", headers.getSendersTimeStamp()));
            sb.append(createJsonPair("receivedTimeStamp", headers.getReceivedTimeStamp()));
            sb.append(createJsonPair("sendingAccessPointPrincipal", (headers.getSendingAccessPointPrincipal() == null) ? null : headers.getSendingAccessPointPrincipal().getName()));
            sb.append(createJsonPair("transmissionId", headers.getTransmissionId()));
            sb.append(createJsonPair("buildUser", OxalisVersion.getUser()));
            sb.append(createJsonPair("buildDescription", OxalisVersion.getBuildDescription()));
            sb.append(createJsonPair("buildTimeStamp", OxalisVersion.getBuildTimeStamp()));
            sb.append("    \"oxalis\" : \"").append(OxalisVersion.getVersion()).append("\"\n");
            sb.append("  }\n}\n");
            return sb.toString();
        } catch (Exception ex) {
            /* default to debug string if JSON marshalling fails */
            return headers.toString();
        }
    }

    private String getSchemeId(ParticipantId participant) {
        String id = "UNKNOWN:SCHEME";
        if (participant != null) {
            String prefix = participant.stringValue().split(":")[0]; // prefix is the first part (before colon)
            SchemeId scheme = SchemeId.fromISO6523(prefix);
            if (scheme != null) {
                id = scheme.getSchemeId();
            } else {
                id = "UNKNOWN:" + prefix;
            }
        }
        return id;
    }

    private String createJsonPair(String key, Object value) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"").append(key).append("\" : ");
        if (value == null) {
            sb.append("null,\n");
        } else {
            sb.append("\"").append(value.toString()).append("\",\n");
        }
        return sb.toString();
    }

    /**
     * Transforms and saves the document as XML
     * @param document the XML document to be transformed
     */
    void saveDocument(Document document, File outputFile) {
        saveDocument(new DOMSource(document), outputFile);
    }

    /**
     * Transforms and saves the stream as XML
     * @param inputStream the XML stream to be transformed
     */
    void saveDocument(InputStream inputStream, File outputFile) {
        saveDocument(new StreamSource(inputStream), outputFile);
    }

    private void saveDocument(Source source, File destination) {
        try {
            FileOutputStream fos = new FileOutputStream(destination);
            Writer writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer;
            transformer = tf.newTransformer();
            transformer.transform(source, result);
            fos.close();
            log.debug("File " + destination + " written");
        } catch (Exception e) {
            throw new SimpleMessageRepositoryException(destination, e);
        }
    }

    @Override
    public String toString() {
        return SimpleMessageRepository.class.getSimpleName();
    }

    /**
     * Computes the directory name for inbound messages.
     * <pre>
     *     /basedir/{recipientId}/{senderId}
     * </pre>
     */
    File computeDirectoryNameForInboundMessage(String inboundMessageStore, ParticipantId recipient, ParticipantId sender) {
        String path = String.format("%s/%s",
                normalize(recipient.stringValue()),
                normalize(sender.stringValue())
            );
        return new File(inboundMessageStore, path);
    }

    String normalize(String s) {
        return s.replaceAll("[:\\/]", "_");
    }

}
