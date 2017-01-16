package eu.peppol.persistence.file;

import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.persistence.RepositoryConfiguration;
import eu.peppol.persistence.TransferDirection;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *   Computes the path for the various artifacts stored in the file system based upon the supplied {@link FileRepoKey}, which holdes
 *   the metadata used in the key.
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

    public Path createPayloadPathFrom(FileRepoKey fileRepoKey) {

        String filename = createBaseFilename(fileRepoKey, ArtifactType.PAYLOAD.getFileNameSuffix());

        Path resolvedPath = createCompletePath(fileRepoKey, filename);

        return resolvedPath;
    }

    public Path createNativeEvidencePathFrom(FileRepoKey fileRepoKey) {

        String fileName = createBaseFilename(fileRepoKey, ArtifactType.NATIVE_EVIDENCE.getFileNameSuffix());
        return createCompletePath(fileRepoKey, fileName);
    }

    String createBaseFilename(FileRepoKey fileRepoKey, String suffix) {
        return normalizeFilename(fileRepoKey.getMessageId().toString()) + suffix;
    }

    Path createCompletePath(FileRepoKey fileRepoKey, String filename) {
        if (fileRepoKey == null) {
            throw new IllegalArgumentException("PeppolTransmissionMetaData is required argument");
        }
        if (filename == null) {
            throw new IllegalArgumentException("filename is required argument");
        }

        if (fileRepoKey.getReceiver() == null) {
            throw new IllegalArgumentException("recipientId is required property on PeppolTransmissionMetaData");
        }
        if (fileRepoKey.getSender() == null) {
            throw new IllegalArgumentException("senderId is required property on PeppolTransmissionMetaData");
        }

        if (fileRepoKey.getDate() == null) {
            throw new IllegalArgumentException("receivedTimeStamp is required property on PeppolTransmissionMetaData");
        }

        Path basePath = createBasePath(fileRepoKey.direction);
        Path path = Paths.get(basePath.toString(),normalizeFilename(fileRepoKey.getReceiver().stringValue()), normalizeFilename(fileRepoKey.getSender().stringValue()), isoDateFormat.format(fileRepoKey.getDate()));
        return path.resolve(filename);
    }

    public Path createBasePath(TransferDirection transferDirection) {
        return Paths.get(basePath.toString(), transferDirection.name());
    }

    public static String normalizeFilename(String s) {
        return s.replaceAll("[^a-zA-Z0-9.-]", "_"); // allow alpha-numericals, punctation and minus (all others will be replaced by underlines)
    }


    public static class FileRepoKey {
        private final TransferDirection direction;
        private final MessageId messageId;
        private final ParticipantId sender;
        private final ParticipantId receiver;
        private final LocalDateTime date;

        public FileRepoKey(TransferDirection direction, MessageId messageId, ParticipantId sender, ParticipantId receiver, LocalDateTime date) {
            this.direction = direction;
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
