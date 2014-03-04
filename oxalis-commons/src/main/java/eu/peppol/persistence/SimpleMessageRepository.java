package eu.peppol.persistence;

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.OxalisVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Default implementation of MessageRepository supplied as part of the Oxalis distribution.
 * Received messages are stored in the file system using JSON and XML.
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


    public void saveInboundMessage(String inboundMessageStore, PeppolMessageMetaData peppolMessageMetaData, Document document) throws OxalisMessagePersistenceException {
        log.info("Default message handler " + peppolMessageMetaData);

        File messageDirectory = prepareMessageDirectory(inboundMessageStore, peppolMessageMetaData.getRecipientId(), peppolMessageMetaData.getSenderId());

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
        log.info("Saving inbound message using " + SimpleMessageRepository.class.getSimpleName());
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
            sb.append(createJsonPair("senderId", headers.getSenderId()));
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
            sb.append("    \"oxalis\" : \"").append(OxalisVersion.getVersion()).append("\"\n");
            sb.append("  }\n}\n");
            return sb.toString();
        } catch (Exception ex) {
            /* default to debug string if JSON marshalling fails */
            return headers.toString();
        }
    }

    private String createJsonPair(String key, Object value) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"" + key + "\" : ");
        if (value == null) {
            sb.append("null,\n");
        } else {
            String v = value.toString();
            sb.append("\"" + value.toString() + "\",\n");
        }
        return sb.toString();
    }

    /**
     * Transforms and saves the document as XML
     * @param document the XML document to be transformed
     */
    void saveDocument(Document document, File outputFile) {
        try {

            FileOutputStream fos = new FileOutputStream(outputFile);
            Writer writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer;
            transformer = tf.newTransformer();
            transformer.transform(new DOMSource(document), result);
            fos.close();

            log.debug("File " + outputFile + " written");

        } catch (Exception e) {
            throw new SimpleMessageRepositoryException(outputFile, e);
        }
    }

    void saveDocument(InputStream inputStream, File outputFile) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outputFile);
            int c;
            while ((c = inputStream.read()) != -1) {
                fileOutputStream.write(c);
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to open/create file " + outputFile + " "+e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write data to " + outputFile + "; " + e.getMessage(), e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to close file", e);
                }
            }
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
