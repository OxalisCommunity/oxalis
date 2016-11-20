package eu.peppol.persistence.jdbc;

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.identifier.*;
import eu.peppol.persistence.*;
import eu.peppol.persistence.api.PeppolPrincipal;
import eu.peppol.persistence.file.ArtifactPathComputer;
import eu.peppol.persistence.file.ArtifactType;
import eu.peppol.persistence.guice.jdbc.JdbcTxManager;
import eu.peppol.persistence.guice.jdbc.Repository;
import eu.peppol.persistence.jdbc.util.MessageMetaDataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MessageRepository implementation which will store the supplied messages in the file system and the meta data into a H2 database.
 *
 * @author Steinar Overbeck Cook
 * @author Thore Holmberg Johnsen
 */
@Repository
public class MessageRepositoryH2Impl implements MessageRepository {

    private static final Logger log = LoggerFactory.getLogger(MessageRepositoryH2Impl.class);

    private final JdbcTxManager jdbcTxManager;
    private final ArtifactPathComputer artifactPathComputer;


    /**
     * This constructor is required for the META-INF/services idiom
     */
    @Inject
    public MessageRepositoryH2Impl(JdbcTxManager jdbcTxManager, ArtifactPathComputer artifactPathComputer) {
        this.jdbcTxManager = jdbcTxManager;
        this.artifactPathComputer = artifactPathComputer;
    }


    /**
     * Saves an outbound message received from the back-end to the file system, with meta data saved into the DBMS.
     *
     * @param messageMetaData
     * @param payloadInputStream
     * @return
     */
    public Long saveOutboundMessage(MessageMetaData messageMetaData, InputStream payloadInputStream) throws OxalisMessagePersistenceException {

        if (messageMetaData.getAccountId() == null) {
            throw new IllegalArgumentException("Outbound messages from back-end must have account id");
        }

        ArtifactPathComputer.FileRepoKey fileRepoKey = fileRepoKeyFrom(messageMetaData);

        Path documentPath = persistArtifact(ArtifactType.PAYLOAD, payloadInputStream, fileRepoKey);

        return createMetaDataEntry(messageMetaData, documentPath.toUri());
    }

    @Override
    public Long saveOutboundMessage(MessageMetaData messageMetaData, Document payloadDocument) throws OxalisMessagePersistenceException {

        if (messageMetaData.getAccountId() == null) {
            throw new IllegalArgumentException("Outbound messages from back-end must have account id");
        }
        ArtifactPathComputer.FileRepoKey fileRepoKey = fileRepoKeyFrom(messageMetaData);

        Path documentPath = persistArtifactFromDocument(ArtifactType.PAYLOAD, payloadDocument, fileRepoKey);

        return createMetaDataEntry(messageMetaData, documentPath.toUri());
    }


    /**
     * Saves inbound messages from PEPPOL network.
     * <p>
     * An attempt is made to locate the associated account by the receivers {@link ParticipantId} supplied in the meta data
     *
     * @param payloadInputStream
     * @return
     * @throws OxalisMessagePersistenceException
     */
    @Override
    public Long saveInboundMessage(MessageMetaData messageMetaData, InputStream payloadInputStream) throws OxalisMessagePersistenceException {

        ArtifactPathComputer.FileRepoKey fileRepositoryMetaData = fileRepoKeyFrom(messageMetaData.getMessageId(), TransferDirection.IN, messageMetaData.getSender(), messageMetaData.getReceiver(), messageMetaData.getReceived());

        // Saves the payload to the file store
        Path documentPath = persistArtifact(ArtifactType.PAYLOAD, payloadInputStream, fileRepositoryMetaData);
        URI payloadUrl = documentPath.toUri();

        // Locates the account for which the received message should be attached to.
        AccountId account = srAccountIdForReceiver(messageMetaData.getReceiver());
        if (account == null) {
            log.warn("Message from " + messageMetaData.getSender() + " will be persisted without account_id");
        } else {
            log.info("Inbound message from " + messageMetaData.getSender() + " will be saved to account " + account);
            messageMetaData.setAccountId(account);
        }

        return createMetaDataEntry(messageMetaData, payloadUrl);
    }


    /**
     * Saves the payload to the file store, finds the account by looking up the receivers participant id and creates a new meta data entry.
     */
    @Override
    public Long saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream payloadInputStream) throws OxalisMessagePersistenceException {

        MessageMetaData messageMetaData = MessageMetaDataHelper.createMessageMetaDataFrom(peppolMessageMetaData);

        return saveInboundMessage(messageMetaData, payloadInputStream);
    }


    @Override
    public void saveInboundTransportReceipt(TransmissionEvidence transmissionEvidence, PeppolMessageMetaData peppolMessageMetaData) throws OxalisMessagePersistenceException {
        TransferDirection transferDirection = TransferDirection.IN;

        log.info("Transmission evidence data to be persisted");

        ArtifactPathComputer.FileRepoKey fileRepoKey = fileRepoKeyFrom(transferDirection, peppolMessageMetaData);

        Path genericEvidencePath = persistArtifact(ArtifactType.GENERIC_EVIDENCE, transmissionEvidence.getInputStream(), fileRepoKey);
        Path nativeEvidencePath = persistArtifact(ArtifactType.NATIVE_EVIDENCE, transmissionEvidence.getNativeEvidenceStream(), fileRepoKey);

        updateMetadataForEvidence(transferDirection , peppolMessageMetaData.getMessageId(), genericEvidencePath, nativeEvidencePath);
    }

    @Override
    public void saveOutboundTransportReceipt(TransmissionEvidence transmissionEvidence, MessageId messageId) {
        TransferDirection transferDirection = TransferDirection.OUT;

        Optional<MessageMetaData> messageMetaDataOptional = findByMessageId(transferDirection, messageId);

        if (messageMetaDataOptional.isPresent()) {
            MessageMetaData mmd = messageMetaDataOptional.get();
            ArtifactPathComputer.FileRepoKey fileRepoKey = fileRepoKeyFrom(messageId, transferDirection, mmd.getSender(), mmd.getReceiver(), mmd.getReceived());
            try {
                Path genericEvidencePath = persistArtifact(ArtifactType.GENERIC_EVIDENCE, transmissionEvidence.getInputStream(), fileRepoKey);
                Path nativeEvidencePath = persistArtifact(ArtifactType.NATIVE_EVIDENCE, transmissionEvidence.getNativeEvidenceStream(), fileRepoKey);

                updateMetadataForEvidence(transferDirection, messageId, genericEvidencePath, nativeEvidencePath);
            } catch (OxalisMessagePersistenceException e) {
                throw new IllegalStateException("Unable to persist generic transport evidence for messageId=" + messageId + ", reason:" + e.getMessage(), e);
            }
        } else
            throw new IllegalStateException("Can not persist generic transport evidence for non-existent messageId " + messageId);
    }

    @Override
    public MessageMetaData findByMessageNo(Long msgNo) {
        if (msgNo == null) {
            throw new IllegalArgumentException("msgNo parameter required");
        }

        String sql = "select * from message where msg_no=?";
        Connection connection = jdbcTxManager.getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, msgNo);

            MessageMetaData result = null;
            ResultSet rs = preparedStatement.executeQuery();

            List<MessageMetaData> messageMetaDataList = messageMetaDataFrom(rs);

            if (messageMetaDataList.size() == 1) {
                result = messageMetaDataList.get(0);
            } else if (messageMetaDataList.size() > 1) {
                throw new IllegalStateException("More than a single entry found for messageNo " + msgNo);
            } else if (messageMetaDataList.isEmpty()) {
                throw new IllegalStateException("Message no " + msgNo + " not found");
            }

            return result;

        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving msg " + msgNo + " using " + sql + "\n" + e.getMessage(), e);
        }
    }


    @Override
    public Optional<MessageMetaData> findByMessageId(TransferDirection transferDirection, MessageId messageId) {

        List<MessageMetaData> byMessageId = findByMessageId(messageId);
        List<MessageMetaData> messageMetaDataList = byMessageId.stream().filter(messageMetaData -> messageMetaData.getTransferDirection() == transferDirection).collect(Collectors.toList());

        Optional<MessageMetaData> result = Optional.empty();

        if (messageMetaDataList.size() > 1) {
            throw new IllegalStateException("More than a single message entry found for messageId=" + messageId);
        } else if (messageMetaDataList.size() == 1) {
            result = Optional.of(messageMetaDataList.get(0));
        }

        return result;

    }

    @Override
    public List<MessageMetaData> findByMessageId(MessageId messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("Argument messageId is required");
        }

        String sql = "select * from message where message_uuid=?";
        Connection con = jdbcTxManager.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, messageId.stringValue());
            ResultSet rs = ps.executeQuery();

            return messageMetaDataFrom(rs);


        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed: " + e.getMessage(), e);
        }
    }

    // Helper methods
    ArtifactPathComputer.FileRepoKey fileRepoKeyFrom(TransferDirection transferDirection, PeppolMessageMetaData peppolMessageMetaData) {
        return fileRepoKeyFrom(new MessageId(peppolMessageMetaData.getMessageId().toString()),
                transferDirection,
                peppolMessageMetaData.getSenderId(), peppolMessageMetaData.getRecipientId(),
                LocalDateTime.ofInstant(peppolMessageMetaData.getReceivedTimeStamp().toInstant(), ZoneId.systemDefault()));
    }

    private ArtifactPathComputer.FileRepoKey fileRepoKeyFrom(MessageId messageId, TransferDirection transferDirection, ParticipantId sender, ParticipantId receiver, LocalDateTime received) {
        return new ArtifactPathComputer.FileRepoKey(transferDirection, messageId, sender, receiver, received);
    }


    private ArtifactPathComputer.FileRepoKey fileRepoKeyFrom(MessageMetaData messageMetaData) {
        return new ArtifactPathComputer.FileRepoKey(messageMetaData.getTransferDirection(), messageMetaData.getMessageId(), messageMetaData.getSender(), messageMetaData.getReceiver(), messageMetaData.getReceived());
    }

    /**
     * Retrieves {@link MessageMetaData} instances from the provided {@link ResultSet}
     *
     * @param rs the result set as returned from the  {@link PreparedStatement#executeQuery()}
     * @return a list of {@link MessageMetaData}, which is empty if the result set is empty
     * @throws SQLException if any of the JDBC calls go wrong
     */
    protected List<MessageMetaData> messageMetaDataFrom(ResultSet rs) throws SQLException {

        List<MessageMetaData> result = new ArrayList<>();
        while (rs.next()) {
            TransferDirection direction = TransferDirection.valueOf(rs.getString("direction"));
            ParticipantId sender = ParticipantId.valueOf(rs.getString("sender"));
            ParticipantId receiver = ParticipantId.valueOf(rs.getString("receiver"));
            PeppolDocumentTypeId document_id = PeppolDocumentTypeId.valueOf(rs.getString("document_id"));
            PeppolProcessTypeId process_id = PeppolProcessTypeId.valueOf(rs.getString("process_id"));


            MessageMetaData.Builder builder = new MessageMetaData.Builder(direction, sender, receiver, document_id, ChannelProtocol.valueOf(rs.getString("channel")));
            builder.accountId(rs.getInt("account_id"))
                    .processTypeId(process_id)
                    .messageNumber(rs.getLong("msg_no"));

            // Received time stamp should never be null, but just in case.
            Timestamp received = rs.getTimestamp("received");
            if (received != null) {

                LocalDateTime receivedLocalDt = LocalDateTime.ofInstant(received.toInstant(), ZoneId.systemDefault());

                builder.received(receivedLocalDt);
            } else
                throw new IllegalStateException("Column received should never be null!");

            Timestamp delivered = rs.getTimestamp("delivered");
            if (delivered != null) {
                LocalDateTime dlv = LocalDateTime.ofInstant(delivered.toInstant(), ZoneId.systemDefault());
                builder.delivered(dlv);
            }
            builder.accountId(rs.getInt("account_id"));
            builder.apPrincipal(new PeppolPrincipal(rs.getString("ap_name")));
            builder.messageId(new MessageId(rs.getString("message_uuid")));
            builder.accessPointIdentifier(new AccessPointIdentifier(rs.getString("remote_host")));
            builder.payloadUri(URI.create(rs.getString("payload_url")));

            String generic_evidence_url = rs.getString("generic_evidence_url");
            if (generic_evidence_url != null) {
                builder.genericEvidenceUri(URI.create(generic_evidence_url));
            }
            String native_evidence_url = rs.getString("native_evidence_url");
            if (native_evidence_url != null) {
                builder.nativeEvidenceUri(URI.create(native_evidence_url));
            }
            MessageMetaData messageMetaData = builder.build();
            result.add(messageMetaData);
        }

        return result;
    }


    private Long createMetaDataEntry(MessageMetaData mmd, URI payloadUrl) {
        if (mmd == null) {
            throw new IllegalArgumentException("MessageMetaData required argument");
        }
        //
        //                                                            1           2           3       4       5       6               7           8           9           10          11           12
        final String INSERT_INTO_MESSAGE_SQL = "insert into message (account_id, direction, sender, receiver, channel, message_uuid, document_id, process_id, ap_name, payload_url, received, delivered ) values(?,?,?,?,?,?,?,?,?,?,?,?)";

        Connection connection = null;
        try {

            log.debug("Creating meta data entry:" + mmd);
            connection = jdbcTxManager.getConnection();

            PreparedStatement insertStatement = connection.prepareStatement(INSERT_INTO_MESSAGE_SQL, Statement.RETURN_GENERATED_KEYS);
            if (mmd.getAccountId() == null)
                insertStatement.setNull(1, Types.INTEGER);
            else
                insertStatement.setInt(1, mmd.getAccountId().toInteger());

            insertStatement.setString(2, mmd.getTransferDirection().name());
            insertStatement.setString(3, mmd.getSender() != null ? mmd.getSender().stringValue() : null);
            insertStatement.setString(4, mmd.getReceiver() != null ? mmd.getReceiver().stringValue() : null);
            insertStatement.setString(5, mmd.getChannelProtocol().name());
            insertStatement.setString(6, mmd.getMessageId().stringValue());     // Unique id of message not to be mixed up with transmission id
            insertStatement.setString(7, mmd.getDocumentTypeId().toString());
            insertStatement.setString(8, mmd.getProcessTypeId() != null ? mmd.getProcessTypeId().toString() : (null));   // Optional
            insertStatement.setString(9, mmd.getAccessPointIdentifier() != null ? mmd.getAccessPointIdentifier().toString() : null); // Optional
            insertStatement.setString(10, payloadUrl.toString());

            insertStatement.setTimestamp(11, java.sql.Timestamp.valueOf(mmd.getReceived()));

            if (mmd.getDelivered() != null) {
                insertStatement.setTimestamp(12, java.sql.Timestamp.valueOf(mmd.getDelivered()));
            } else
                insertStatement.setTimestamp(12, null);

            insertStatement.executeUpdate();

            long generatedKey = 0;

            boolean supportsGetGeneratedKeys = connection.getMetaData().supportsGetGeneratedKeys();
            log.debug("Supports generated keys: " + supportsGetGeneratedKeys);

            if (supportsGetGeneratedKeys) {
                ResultSet rs = insertStatement.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    generatedKey = rs.getLong(1);

                    log.debug("Inserted message with msg_no: " + generatedKey + " into table 'message'");
                } else {
                    log.debug("Inserted message into table 'message', but auto generated keys is not supported");
                }
            } else {
                log.debug("Inserted message into table 'message', auto generated keys not supported ");
            }

            insertStatement.close();

            log.info("Autocommit set to : " + connection.getAutoCommit());

            return generatedKey;

        } catch (Exception e) {
            log.error("Unable to insert into message table using " + INSERT_INTO_MESSAGE_SQL + ", " + e, e);
            log.error("Please ensure that the DBMS and the MESSAGE table is available.");
            throw new IllegalStateException("Unable to create new entry in MESSAGE " + e.getMessage(), e);
        }
    }


    /**
     * Persists a payload represented as a W3C Document to the file system based upon the meta data
     *
     * @param artifactType
     * @param payloadDocument the payload represented as a W3C Document
     * @param fileRepoKey
     * @return
     */
    Path persistArtifactFromDocument(ArtifactType artifactType, Document payloadDocument, ArtifactPathComputer.FileRepoKey fileRepoKey) {

        Path path = createDirectoryForArtifact(artifactType, fileRepoKey);
        log.debug("Writing w3c document to " + path);

        DOMSource domSource = new DOMSource(payloadDocument);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            StreamResult streamResult = new StreamResult(Files.newBufferedWriter(path, Charset.forName("UTF-8")));
            transformer.transform(domSource, streamResult);
        } catch (TransformerException | IOException e) {
            throw new IllegalStateException("Unable to write xml document to " + path + ". " + e.getMessage(), e);
        }
        return path;
    }

    Path persistArtifact(ArtifactType artifactType, InputStream inputStream, ArtifactPathComputer.FileRepoKey fileRepoKey) throws OxalisMessagePersistenceException {

        Path documentPath = createDirectoryForArtifact(artifactType, fileRepoKey);
        try {
            Files.copy(inputStream, documentPath);
            log.info(artifactType.getDescription() + " copied to " + documentPath);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save artifact to " + documentPath, e);
        }
        return documentPath;
    }

    Path createDirectoryForArtifact(ArtifactType artifactType, ArtifactPathComputer.FileRepoKey fileRepoKey) {
        Function<ArtifactPathComputer.FileRepoKey, Path> function = getFileRepoMetaDataPathFunction(artifactType);
        Path path = function.apply(fileRepoKey);
        verifyAndCreateDirectories(path);
        return path;
    }


    /**
     * Figures out which {@link Function} to apply for a given instance of {@link ArtifactType}
     *
     * @param artifactType the artifact type for which a function to apply should be determined.
     * @return the path computing function.
     */
    private Function<ArtifactPathComputer.FileRepoKey, Path> getFileRepoMetaDataPathFunction(ArtifactType artifactType) {

        Function<ArtifactPathComputer.FileRepoKey, Path> function;
        switch (artifactType) {
            case GENERIC_EVIDENCE:
                function = artifactPathComputer::createGenericEvidencePathFrom;
                break;
            case NATIVE_EVIDENCE:
                function = artifactPathComputer::createNativeEvidencePathFrom;
                break;
            case PAYLOAD:
                function = artifactPathComputer::createPayloadPathFrom;
                break;
            default:
                throw new IllegalStateException("No implementation for artifact type " + artifactType.name());
        }
        return function;
    }


    private void updateMetadataForNativeEvidence(TransferDirection transferDirection, MessageId messageId, Path path) {

        String dateColumnName = dateColumnNameFor(transferDirection);

        String sql = "update message set " + ArtifactType.NATIVE_EVIDENCE.getColumnName() + "=? "   // p1
                + ", " + dateColumnName + "=? " // p2
                + " where message_uuid=? and direction=?";  // p3 & p4
        try {
            Connection con = jdbcTxManager.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, path.toUri().toString());
            ps.setTimestamp(2, new Timestamp(new java.util.Date().getTime()));
            ps.setString(3, messageId.stringValue());
            ps.setString(4, transferDirection.name());
            int i = ps.executeUpdate();
            if (i != 1) {
                throw new IllegalStateException("Execution of " + sql + " for messagId " + messageId + " modified 0 rows");
            }
        } catch (SQLException e) {
            log.error(sql + " failed: " + e.getMessage(), e);
            throw new IllegalStateException("Unable to update message: " + e.getMessage(), e);
        }
    }

    private void updateMetadataForEvidence(TransferDirection transferDirection, MessageId messageId, Path genericEvidencePath, Path nativeEvidencePath) {

        String dateColumnName = dateColumnNameFor(transferDirection);

        String sql = "update message set " + ArtifactType.GENERIC_EVIDENCE.getColumnName() + " = ?, "   // p1
                + ArtifactType.NATIVE_EVIDENCE.getColumnName()+ "=?, " // p2
                + dateColumnName + "=? " // p3
                + " where message_uuid = ? and direction=?"; // p4 & p5

        log.debug("Updating meta data: " + sql);
        Connection con = null;
        try {
            con = jdbcTxManager.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, genericEvidencePath.toUri().toString());
            ps.setString(2, nativeEvidencePath.toUri().toString());
            ps.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
            ps.setString(4, messageId.stringValue());
            ps.setString(5, transferDirection.name());
            int i = ps.executeUpdate();
            if (i != 1) {
                throw new IllegalStateException("Unable to update message table for message_uuid=" + messageId);
            }
            con.commit();

        } catch (SQLException e) {
            log.error("Unable to update message table." + e.getMessage(), e);
            throw new IllegalStateException("Unable to update database for storing genric and native evidene for message " + messageId, e);
        }
    }

    private String dateColumnNameFor(TransferDirection transferDirection) {
        String dateColumnName = null;
        switch (transferDirection) {
            case IN:
                dateColumnName = "received";
                break;
            case OUT:
                dateColumnName = "delivered";
                break;
            default:
                throw new IllegalArgumentException("Unknown transferDirection: " + transferDirection);
        }
        return dateColumnName;
    }

    private void verifyAndCreateDirectories(Path documentPath) {
        Path directory = documentPath.getParent();
        if (Files.notExists(directory) || Files.isDirectory(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create directories for path " + directory);
            }
        }
    }


    private AccountId findAccountIdByReceiver(PeppolMessageMetaData peppolMessageMetaData) {
        // Find the account identification for the receivers participant id
        AccountId account = srAccountIdForReceiver(peppolMessageMetaData.getRecipientId());
        if (account == null) {
            log.error("Unable to find account for participant " + peppolMessageMetaData.getRecipientId());
        }
        return account;
    }


    // Package private to ease testing
    //

    AccountId srAccountIdForReceiver(ParticipantId participantId) {

        if (participantId == null) {
            return null;
        }

        Integer accountId = null;

        Connection con = null;

        try {
            con = jdbcTxManager.getConnection();
            PreparedStatement ps = con.prepareStatement("select account_id from account_receiver where participant_id=?");
            ps.setString(1, participantId.stringValue());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                accountId = rs.getInt(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            log.error("Unable to obtain the account id for participant " + participantId + "; reason:" + e.getMessage());
        }

        return new AccountId(accountId);
    }

}
