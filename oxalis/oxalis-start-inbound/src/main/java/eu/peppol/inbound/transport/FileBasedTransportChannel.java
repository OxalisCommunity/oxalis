/*
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
package eu.peppol.inbound.transport;

import eu.peppol.inbound.soap.SOAPHeaderDocument;
import eu.peppol.inbound.util.Log;
import eu.peppol.outbound.soap.SoapHeader;
import eu.peppol.start.util.Configuration;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Date;

/**
 * Persists data received for a given transport channel.
 *
 * @author Jose Gorvenia Narvaez(jose@alfa1lab.com)
 * @author Steinar Overbeck Cook (steinar@sendregning.eu)
 */
public class FileBasedTransportChannel  {

    public static final String EXT_METADATA = ".metadata.xml";
    public static final String EXT_PAYLOAD = ".payload.xml";
    public static final String INBOX_DIR = "inbox";
    public static final long MESSAGE_INVALID_TIME_IN_MILLIS = 1000L * 60L * 60L * 2L;
    protected String storePath;
    public boolean isSaved = false;
    public boolean isMetadataRemoved = false;
    public boolean isPayloadRemoved = false;

    public FileBasedTransportChannel() {
        this.storePath = Configuration.getInstance().getInboundMessageStore();
    }

    public final void saveDocument(SoapHeader soapHeader, Document payloadDocument) {
        String channelID = soapHeader.getRecipient();
        String messageID = soapHeader.getMessageIdentifier();

        // Creates XML document holding the meta data about the message
        Document metadataDocument = SOAPHeaderDocument.create(soapHeader);

        isSaved = false;

        File channelInboxDir = getChannelInboxDir(channelID);

        File metadataFile = getMetadataFile(channelInboxDir, messageID);
        File payloadFile = getPayloadFile(channelInboxDir, messageID);

        try {
            if (!metadataFile.createNewFile()) {
                throw new IllegalStateException("Metadata file exists for message ID "
                        + messageID
                        + " in inbox for channel "
                        + channelID);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create meta data file for messageID " + messageID, e);
        }

        try {
            if (!payloadFile.createNewFile()) {
                Log.error("Payload file exists for message ID "
                        + messageID
                        + " in inbox for channel "
                        + channelID);
                if (!metadataFile.delete()) {
                    Log.info("Cannot delete metadata file for message ID "
                            + messageID
                            + " in inbox for channel "
                            + channelID);
                } else {
                    Log.info("Metadata file deleted: " + metadataFile.getAbsolutePath());
                }

                throw new IllegalStateException("Cannot create new payload file for message ID "
                        + messageID
                        + " in inbox for channel "
                        + channelID);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create new payload file for message ID " + messageID + " in inbox for channel " + channelID, e);
        }

        try {

            writeDocumentToFile(metadataFile, metadataDocument);
            Log.info("Metadata created: " + metadataFile.getName());
            writeDocumentToFile(payloadFile, payloadDocument);
            Log.info("Payload created: " + payloadFile.getName());

            isSaved = true;
        } catch (RuntimeException e) {
            if (metadataFile.delete()) {
                Log.info("Metadata file deleted: " + metadataFile.getAbsolutePath());
            } else {
                Log.info("Cannot delete Metadata file: " + metadataFile.getAbsolutePath());
            }
            if (payloadFile.delete()) {
                Log.info("Payload file deleted: " + payloadFile.getAbsolutePath());
            } else {
                Log.info("Cannot delete Payload file: " + payloadFile.getAbsolutePath());
            }

            Log.error("Error saving a document.", e);

            throw e;
        }
    }

    /**
     * Delete a Document.
     *
     * @param channelID ChannelID directory.
     * @param messageID ID of the Message.
     * @throws Exception Throw the exception.
     */
    public final void deleteDocument(final String channelID,
                                     final String messageID) throws Exception {

        if (channelID != null && messageID != null) {
            File channelInboxDir = getChannelInboxDir(channelID);
            File metadataFile = getMetadataFile(channelInboxDir, messageID);
            File payloadFile = getPayloadFile(channelInboxDir, messageID);

            if (metadataFile.exists()) {
                if (metadataFile.delete()) {
                    isMetadataRemoved = true;
                    Log.info("Metadata file deleted: " + metadataFile.getAbsolutePath());
                } else {
                    Log.info("Cannot delete Metadata file: " + metadataFile.getAbsolutePath());
                }
            }
            if (payloadFile.exists()) {
                if (payloadFile.delete()) {
                    isPayloadRemoved = true;
                    Log.info("Payload file deleted: " + payloadFile.getAbsolutePath());
                } else {
                    Log.info("Cannot delete Payload file: " + payloadFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Get MessagesID from a Channel.
     *
     * @param channelID ID of the Channel.
     * @return Array of MessagesID.
     * @throws Exception Throws an exception.
     */
    public final String[] getMessageIDs(final String channelID) throws Exception {

        File dir = getChannelInboxDir(channelID);
        File[] files = dir.listFiles(new FilenameFilter() {

            public boolean accept(final File dir, final String name) {
                return (name.endsWith(EXT_PAYLOAD));
            }
        });

        String[] messageIDs = new String[files.length];
        int i = 0;
        for (File payloadFile : files) {
            String curMessageId = getMessageIDFromPayloadFile(payloadFile);

            if ((System.currentTimeMillis() - payloadFile.lastModified())
                    > MESSAGE_INVALID_TIME_IN_MILLIS) {
                deleteDocument(channelID, curMessageId);
            } else {
                messageIDs[i] = curMessageId;
                i++;
            }
        }
        return messageIDs;
    }

    /**
     * Get Metadata of a Document.
     *
     * @param channelID ID of the Channel.
     * @param messageID ID of the Message.
     * @return Metadata Document
     * @throws Exception throws an exception.
     */
    public final Document getDocumentMetadata(final String channelID,
                                              final String messageID) throws Exception {

        File channelInboxDir = getChannelInboxDir(channelID);
        File metadataFile = getMetadataFile(channelInboxDir, messageID);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(metadataFile);
    }

    public final Document getDocument(final String channelID,
                                      final String messageID) throws Exception {

        File channelInboxDir = getChannelInboxDir(channelID);
        File payloadFile = getPayloadFile(channelInboxDir, messageID);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(payloadFile);
    }

    public final long getSize(final String channelID,
                              final String messageID) throws Exception {

        File channelInboxDir = getChannelInboxDir(channelID);
        File payloadFile = getPayloadFile(channelInboxDir, messageID);
        long fileLength = payloadFile.length();
        //calculate length in Kilobytes and round up
        final int kb = 1023;
        final int size = 1024;
        long fileLenghtInKB = (fileLength + kb) / size;
        return fileLenghtInKB;
    }

    public final Date getCreationTime(final String channelID,
                                      final String messageID) throws Exception {

        File channelInboxDir = getChannelInboxDir(channelID);
        File payloadFile = getPayloadFile(channelInboxDir, messageID);
        return new Date(payloadFile.lastModified());
    }

    private String getMessageIDFromPayloadFile(final File payloadFile) {

        String str = payloadFile.getName();

        String messageID =
                str.substring(
                        0, str.length()
                        - EXT_PAYLOAD.length());

        messageID = messageID.replace('_', ':');
        return messageID;
    }

    private File getMetadataFile(final File channelInboxDir, String messageID) {
        messageID = removeSpecialChars(messageID);
        return new File(channelInboxDir, messageID + EXT_METADATA);
    }

    private File getPayloadFile(final File channelInboxDir, String messageID) {
        messageID = removeSpecialChars(messageID);
        File file = new File(channelInboxDir, messageID + EXT_PAYLOAD);

        return file;
    }

    private File getChannelInboxDir(String channelID) {

        File inboxDir = new File(storePath, INBOX_DIR);

        if (!inboxDir.exists()) {
            if (!inboxDir.mkdirs()) {

                Log.info("Cannot create the inbox directory: " + storePath + "/" + inboxDir);
            }
        }

        channelID = removeSpecialChars(channelID);
        File channelDir = new File(inboxDir, channelID);

        if (!channelDir.exists()) {
            if (!channelDir.mkdirs()) {
                Log.info("Cannot create the channel directory: "
                        + inboxDir + "/" + channelID);
            }
        }

        if (!channelDir.exists()) {

            Log.error("Inbox for channel \""
                    + channelID
                    + "\" could not be found or created: "
                    + channelDir.getAbsolutePath());

            throw new IllegalStateException("Inbox for channel \""
                    + channelID
                    + "\" could not be found or created: "
                    + channelDir.getAbsolutePath());
        }

        return channelDir;
    }

    private String removeSpecialChars(String fileOrDirName) {
        fileOrDirName = fileOrDirName.replace(':', '_');
        return fileOrDirName;
    }

    private void writeDocumentToFile(File messageFile, Document document) {


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(bos));
            bos.close();
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Unable to configure transformer for " + messageFile + "; " + e, e);
        } catch (TransformerException e) {
            throw new IllegalStateException("Unable to transform into " + messageFile + "; " + document, e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to close ByteArrayOutputStream " + e, e);
        }


        // %%% puts in a known stylesheet for demo purposes

        String utf8 = "UTF-8";
        String xmlDocument = null;
        try {
            xmlDocument = bos.toString(utf8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to convert Byte Array to String using UTF8 encoding" + e, e);
        }

        xmlDocument = xmlDocument.replace("standalone=\"eu\"?>", "standalone=\"eu\"?><?xml-stylesheet type=\"text/xsl\" href=\"EHF-faktura_NO.xslt\"?>");

        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(messageFile)), utf8);
            writer.write(xmlDocument);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("File " + messageFile + " not found; " + e, e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Encoding into " + utf8 + " failed while writing to " + messageFile, e);
        } catch (IOException e) {
            throw new IllegalStateException("I/O error while writing to " + messageFile, e);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to close output writer for file " + messageFile);
            }
        }

        // %%% puts in a known stylesheet for demo purposes
    }
}
