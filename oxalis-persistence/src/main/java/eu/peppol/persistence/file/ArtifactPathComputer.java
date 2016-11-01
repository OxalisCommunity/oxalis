package eu.peppol.persistence.file;

import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.persistence.RepositoryConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author steinar
 *         Date: 17.10.2016
 *         Time: 11.02
 */
public class ArtifactPathComputer {

    private final Path basePath;

    private DateTimeFormatter isoDateFormat = DateTimeFormatter.ISO_LOCAL_DATE;

    @Inject
    public ArtifactPathComputer(@Named(RepositoryConfiguration.BASE_PATH_NAME) Path basePath) {

        this.basePath = basePath;
    }

    public Path createPayloadPathFrom(FileRepoMetaData fileRepoMetaData) {

        String filename = createBaseFilename(fileRepoMetaData, ArtifactType.PAYLOAD.getFileNameSuffix());

        Path resolvedPath = createCompletePath(fileRepoMetaData, filename);

        return resolvedPath;
    }

    public Path createNativeEvidencePathFrom(FileRepoMetaData peppolMessageMetaData) {

        String fileName = createBaseFilename(peppolMessageMetaData, ArtifactType.NATIVE_EVIDENCE.getFileNameSuffix());
        return createCompletePath(peppolMessageMetaData, fileName);
    }

    public Path createGenericEvidencePathFrom(FileRepoMetaData peppolMessageMetaData) {
        String fileName = createBaseFilename(peppolMessageMetaData, ArtifactType.GENERIC_EVIDENCE.getFileNameSuffix());
        return createCompletePath(peppolMessageMetaData, fileName);
    }

    String createBaseFilename(FileRepoMetaData fileRepoMetaData, String suffix) {
        return normalizeFilename(fileRepoMetaData.getMessageId().toString()) + suffix;
    }

    Path createCompletePath(FileRepoMetaData fileRepoMetaData, String filename) {
        if (fileRepoMetaData == null) {
            throw new IllegalArgumentException("PeppolMessageMetaData is required argument");
        }
        if (filename == null) {
            throw new IllegalArgumentException("filename is required argument");
        }

        if (fileRepoMetaData.getReceiver() == null) {
            throw new IllegalArgumentException("recipientId is required property on PeppolMessageMetaData");
        }
        if (fileRepoMetaData.getSender() == null) {
            throw new IllegalArgumentException("senderId is required property on PeppolMessageMetaData");
        }

        if (fileRepoMetaData.getDate() == null) {
            throw new IllegalArgumentException("receivedTimeStamp is required property on PeppolMessageMetaData");
        }

        Path path = Paths.get(basePath.toString(), normalizeFilename(fileRepoMetaData.getReceiver().stringValue()), normalizeFilename(fileRepoMetaData.getSender().stringValue()), isoDateFormat.format(fileRepoMetaData.getDate()));
        return path.resolve(filename);
    }


    public static String normalizeFilename(String s) {
        return s.replaceAll("[^a-zA-Z0-9.-]", "_"); // allow alpha-numericals, punctation and minus (all others will be replaced by underlines)
    }


    public static class FileRepoMetaData {
        MessageId messageId;
        ParticipantId sender;
        ParticipantId receiver;
        LocalDateTime date;

        public FileRepoMetaData(MessageId messageId, ParticipantId sender, ParticipantId receiver, LocalDateTime date) {
            this.messageId = messageId;
            this.sender = sender;
            this.receiver = receiver;
            this.date = date;
        }

        public MessageId getMessageId() {
            return messageId;
        }

        public ParticipantId getSender() {
            return sender;
        }

        public ParticipantId getReceiver() {
            return receiver;
        }

        public LocalDateTime getDate() {
            return date;
        }
    }

}
