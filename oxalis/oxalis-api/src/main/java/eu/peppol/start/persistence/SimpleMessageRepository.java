package eu.peppol.start.persistence;

import eu.peppol.start.identifier.IdentifierName;
import eu.peppol.start.identifier.PeppolMessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.security.auth.login.Configuration;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Date;
import java.util.Map;

/**
 * @author $Author$ (of last change)
 *         Created by
 *         User: steinar
 *         Date: 28.11.11
 *         Time: 21:09
 */
public class SimpleMessageRepository implements MessageRepository {


    private static final Logger log = LoggerFactory.getLogger(SimpleMessageRepository.class);

    @Override
    public void saveInboundMessage(String inboundMessageStore, PeppolMessageHeader peppolMessageHeader, Document document) {
        log.info("Default message handler " + peppolMessageHeader);

        File messageDirectory = prepareMessageDirectory(inboundMessageStore, peppolMessageHeader);


        try {
            File messageFullPath = new File(messageDirectory, peppolMessageHeader.getMessageId().stringValue().replace(":", "_") + ".xml");
            saveDocument(document, messageFullPath);

            File messageHeaderFilePath = new File(messageDirectory, peppolMessageHeader.getMessageId().stringValue().replace(":", "_") + ".txt");
            saveHeader(peppolMessageHeader, messageHeaderFilePath, messageFullPath);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to persist message " + peppolMessageHeader.getMessageId(), e);
        }

    }

    File prepareMessageDirectory(String inboundMessageStore, PeppolMessageHeader peppolMessageHeader) {
        // Computes the full path of the directory in which message and routing data should be stored.
        File messageDirectory = computeDirectoryNameForMessage(inboundMessageStore, peppolMessageHeader);
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


    File computeDirectoryNameForMessage(String inboundMessageStore, PeppolMessageHeader peppolMessageHeader) {
        if (peppolMessageHeader == null) {
            throw new IllegalArgumentException("peppolMessageHeader required");
        }

        String path = String.format("%s/%s/%s",
                peppolMessageHeader.getRecipientId().stringValue().replace(":", "_"),
                peppolMessageHeader.getChannelId().stringValue(),
                peppolMessageHeader.getSenderId().stringValue().replace(":", "_"));
        File result = new File(inboundMessageStore, path);

        return result;
    }

    void saveHeader(PeppolMessageHeader peppolMessageHeader, File messageHeaderFilerPath, File messageFullPath) {
        try {
            FileOutputStream fos = new FileOutputStream(messageHeaderFilerPath);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));
            Date date = new Date();

            // Formats the current time and date according to the ISO8601 standard.
            pw.append("TimeStamp=").format("%tFT%tT%tz\n", date,date,date);

            pw.append("MessageFileName=").append(messageFullPath.toString());
            pw.append(IdentifierName.MESSAGE_ID.stringValue()).append("=").append(peppolMessageHeader.getMessageId().stringValue()).append('\n');
            pw.append(IdentifierName.CHANNEL_ID.stringValue()).append("=").append(peppolMessageHeader.getChannelId().stringValue()).append('\n');
            pw.append(IdentifierName.RECIPIENT_ID.stringValue()).append('=').append(peppolMessageHeader.getRecipientId().stringValue()).append('\n');
            pw.append(IdentifierName.SENDER_ID.stringValue()).append('=').append(peppolMessageHeader.getSenderId().stringValue()).append('\n');
            pw.append(IdentifierName.DOCUMENT_ID.stringValue()).append('=').append(peppolMessageHeader.getDocumentId().stringValue()).append('\n');
            pw.append(IdentifierName.PROCESS_ID.stringValue()).append('=').append(peppolMessageHeader.getProcessId().stringValue()).append('\n');
            pw.close();
            log.debug("File " + messageHeaderFilerPath + " written");

        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to create file " + messageHeaderFilerPath + "; " + e, e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to create writer for " + messageHeaderFilerPath + "; " + e, e);
        }
    }

    /**
     * Transforms an XML document into a String
     *
     * @param document the XML document to be transformed
     * @return the string holding the XML document
     */
    void saveDocument(Document document, File outputFile) {

        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            Writer writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));

            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = null;
            transformer = tf.newTransformer();
            transformer.transform(new DOMSource(document), result);
            fos.close();
            log.debug("File " + outputFile + " written");
        } catch (Exception e) {
            throw new SimpleMessageRepositoryException(outputFile, e);
        }

    }


    @Override
    public String toString() {
        return SimpleMessageRepository.class.getSimpleName();
    }

}
