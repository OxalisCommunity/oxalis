package eu.peppol.sbdh;

import eu.peppol.PeppolMessageMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author steinar
 *         Date: 24.10.13
 *         Time: 10:16
 */
public class SimpleSbdhMessageRepository implements SbdhMessageRepository {

    public static final Logger log = LoggerFactory.getLogger(SimpleSbdhMessageRepository.class);

    private final String inboundMessageStore;

    public SimpleSbdhMessageRepository(String inboundMessageStore) {
        this.inboundMessageStore = inboundMessageStore;
    }

    @Override
    public void persist(PeppolMessageMetaData transmissionData, InputStream payload) throws SbdhMessageException {

        if (transmissionData == null) {
            throw new IllegalArgumentException("transmissiondata required arg");
        }

        if (payload == null) {
            throw new IllegalArgumentException("Required arg payload missing");
        }


        File messageFile = getMessageFile(transmissionData);

        log.info("Saved incoming message to " + messageFile.getAbsolutePath());
        saveStream(messageFile, payload);

        File transmissionDataFile = getTransmissionDataFile(transmissionData);
        StringWriter stringWriter = new StringWriter();
        stringWriter.write(transmissionData.toString());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringWriter.toString().getBytes());
        log.info("Saved incoming message information to " + transmissionDataFile.getAbsolutePath());
        saveStream(transmissionDataFile, byteArrayInputStream);
    }

    public File getMessageFile(PeppolMessageMetaData transmissionData) {
        File messageDirectory = prepareMessageDirectory(transmissionData);
        String messageFileName = baseFileName(transmissionData) + ".xml";

        return new File(messageDirectory, messageFileName);


    }

    public File getTransmissionDataFile(PeppolMessageMetaData transmissionData) {
        File messageDirectory = prepareMessageDirectory(transmissionData);
        String transmissionDataFileName = baseFileName(transmissionData) + ".txt";

        return new File(messageDirectory, transmissionDataFileName);
    }


    private String baseFileName(PeppolMessageMetaData transmissionData) {
        return normalize(transmissionData.getMessageId());
    }


    private void saveStream(File messageFile, InputStream data) {

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(messageFile);

            int c;
            while ((c = data.read()) >= 0) {
                fileOutputStream.write(c);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    File prepareMessageDirectory(PeppolMessageMetaData transmissionData) {

        String path = String.format("%s/%s/%s",
                normalize(transmissionData.getRecipientId().toString()),
                normalize(transmissionData.getSendingAccessPoint()),
                normalize(transmissionData.getSenderId().toString())
        );
        File dir = new File(inboundMessageStore, path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Unable to create directory " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    private String normalize(String s) {
        return s.replaceAll("[:/\\\\]", "_");
    }
}
