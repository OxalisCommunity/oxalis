/* Created by steinar on 08.01.12 at 21:46 */
package eu.peppol.persistence.jdbc.util;

import com.google.inject.Inject;
import eu.peppol.identifier.MessageId;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.persistence.*;
import eu.peppol.persistence.api.UserName;
import eu.peppol.persistence.api.account.Account;
import eu.peppol.persistence.api.account.AccountRepository;
import eu.peppol.persistence.api.account.Customer;
import eu.peppol.persistence.api.account.CustomerId;
import eu.peppol.persistence.guice.jdbc.JdbcTxManager;
import eu.peppol.persistence.guice.jdbc.Repository;
import eu.peppol.persistence.queue.OutboundMessageQueueErrorId;
import eu.peppol.persistence.queue.OutboundMessageQueueId;
import eu.peppol.persistence.queue.OutboundMessageQueueState;
import eu.peppol.persistence.queue.QueuedOutboundMessageError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class providing helper methods to create messages and accounts for testing purposes.
 * <p>
 * It can be either extended by other integration test or used in http tests
 *
 * @author Adam Mscisz adam@sendregning.no
 * @deprecated use the appropriate Repository classes as this class has numerous unexpected side effects
 */
@Repository
public class DatabaseHelper {

    public static final Logger log = LoggerFactory.getLogger(DatabaseHelper.class);
    private final AccountRepository accountRepository;
    private final JdbcTxManager jdbcTxManager;

    // General persistence layer
    private final MessageRepository messageRepository;

    @Inject
    public DatabaseHelper(AccountRepository accountRepository, JdbcTxManager jdbcTxManager, MessageRepository messageRepository) {
        this.accountRepository = accountRepository;
        this.jdbcTxManager = jdbcTxManager;
        this.messageRepository = messageRepository;
    }


    public Long createMessage(PeppolDocumentTypeId documentId, PeppolProcessTypeId processTypeId, String message, Integer accountId, TransferDirection direction,
                              String senderValue, String receiverValue,
                              final String uuid, Date delivered, Date received) {

        if (received == null) {
            throw new IllegalArgumentException("received date is required");
        }

        MessageMetaData.Builder builder = new MessageMetaData.Builder(TransferDirection.valueOf(direction.name()),
                new ParticipantId(senderValue), new ParticipantId(receiverValue), documentId,
                ChannelProtocol.SREST);

        if (uuid != null && uuid.trim().length() > 0) {
            builder.messageId(new MessageId(uuid));
        }
        if (delivered != null) {
            builder.delivered(LocalDateTime.ofInstant(delivered.toInstant(), ZoneId.systemDefault()));
        }
        if (received != null) {
            builder.received(LocalDateTime.ofInstant(received.toInstant(), ZoneId.systemDefault()));
        }
        if (processTypeId != null) {
            builder.processTypeId(processTypeId);
        }

        if (accountId != null) {
            builder.accountId(accountId);
        }
        MessageMetaData messageMetaData = builder.build();

        try {
            if (messageMetaData.getTransferDirection() == TransferDirection.IN) {
                return messageRepository.saveInboundMessage(messageMetaData, new ByteArrayInputStream(message.getBytes(Charset.forName("UTF-8"))));
            } else if (messageMetaData.getTransferDirection() == TransferDirection.OUT) {
                return messageRepository.saveOutboundMessage(messageMetaData, new ByteArrayInputStream(message.getBytes(Charset.forName("UTF-8"))));
            } else
                throw new IllegalStateException("No support for transfer direction " + messageMetaData.getTransferDirection().name());
        } catch (OxalisMessagePersistenceException e) {
            throw new IllegalStateException("Unable to save message " + e.getMessage());
        }

    }

    /**
     * Helper method creating simple sample message. If the direction is {@link TransferDirection#IN} the accountId
     * parameter will be ignored. The accountId will be set based upon the contents of the {@code account_receiver} table in
     * the database.
     *
     * @param direction indicates whether the message is inbound or outbound with respect to the PEPPOL network.
     */
    public Long createMessage(Integer accountId, TransferDirection direction, String senderValue, String receiverValue, final String uuid, Date delivered, PeppolDocumentTypeId peppolDocumentTypeId, PeppolProcessTypeId peppolProcessTypeId) {
        PeppolDocumentTypeId invoiceDocumentType =peppolDocumentTypeId;
        PeppolProcessTypeId processTypeId = peppolProcessTypeId;

        return createMessage(invoiceDocumentType, processTypeId, "<test>\u00E5</test>", accountId, direction, senderValue, receiverValue, uuid, delivered, new Date());
    }

    /**
     * Helper method to delete rows in message table
     *
     * @param msgNo
     */

    public void deleteMessage(Long msgNo) {

        if (msgNo == null) {
            return;
        }

        Connection con = null;
        String sql = "delete from message where msg_no = ?";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, msgNo);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    /**
     * Helper method updating received date on message
     *
     * @param date
     * @param msgNo
     */
    public void updateMessageDate(Date date, Long msgNo) {
        Connection con = null;
        String sql = "update message set received = ? where msg_no = ?";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(date.getTime()));
            ps.setLong(2, msgNo);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }


    public void deleteAllMessagesForAccount(Account account) {
        if (account == null || account.getAccountId() == null) {
            return;
        }

        Connection con = null;
        String sql = "delete from message where account_id = ?";

        try {
            con = jdbcTxManager.getConnection();

            // Delete artifacts first.
            PreparedStatement ps = con.prepareStatement("select * from message where account_id = ?");
            ps.setInt(1, account.getAccountId().toInteger());
            ResultSet rs = ps.executeQuery();
            deleteArtifacts(rs);
            ps.close();

            ps = con.prepareStatement(sql);
            ps.setInt(1, account.getAccountId().toInteger());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    public void deleteAllMessagesWithoutAccountId() {
        Connection con = null;
        String sql = "delete from message where account_id is null";

        try {
            con = jdbcTxManager.getConnection();
            // Delete artifacts first.
            PreparedStatement ps = con.prepareStatement("select * from message where account_id is null");
            ResultSet rs = ps.executeQuery();
            deleteArtifacts(rs);
            ps.close();

            ps = con.prepareStatement(sql);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    private void deleteIfExists(String payloadUrl) throws IOException {
        if (payloadUrl != null) {
            Files.deleteIfExists(Paths.get(URI.create(payloadUrl)));
        }
    }

    private void deleteArtifacts(ResultSet rs) {
        try {
            while (rs.next()) {
                deleteIfExists(rs.getString("payload_url"));
                deleteIfExists(rs.getString("native_evidence_url"));
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Error while deleting artifacts" + e, e);
        }

    }

    public void updateMessageReceiver(Long msgNo, String receiver) {

        Connection con = null;
        String sql = "update message set receiver = ? where msg_no = ?";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, receiver);
            ps.setLong(2, msgNo);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }

    }

    public int addAccountReceiver(AccountId id, String receiver) {

        Connection con = null;
        String sql = "insert into account_receiver (account_id, participant_id) values(?,?)";

        try {
            con = jdbcTxManager.getConnection();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, id.toInteger());
            ps.setString(2, receiver);

            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int accountReceiverId = rs.getInt(1);

                return accountReceiverId;
            } else {
                throw new IllegalStateException("Unable to obtain generated key after insert.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    public void deleteAccountReceiver(Integer accountReceiverId) {
        if (accountReceiverId == null) {
            return;
        }

        Connection con = null;
        String sql = "delete from account_receiver where id = ?";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accountReceiverId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    public void deleteCustomer(Customer customer) {
        if (customer == null) {
            return;
        }
        Connection con = null;
        String sql = "delete from customer where id = ?";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, customer.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    /**
     * Deletes all data related to an account.
     *
     * @param userNameToBeDeleted - it's both account.username and customer.name
     */
    public void deleteAccountData(UserName userNameToBeDeleted) {

        String userName = userNameToBeDeleted.stringValue();

        CustomerId customerIdToBeDeleted = null;

        Connection con = null;
        String sql = "delete from account_role where username = ?";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, userName);
            ps.executeUpdate();

            sql = "delete from account_receiver where account_id = (select id from account where username = ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, userName);
            ps.executeUpdate();

            sql = "select customer_id from account where username = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, userName);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                customerIdToBeDeleted = new CustomerId(resultSet.getInt(1));
            }

            sql = "delete from account where username = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, userName);
            ps.executeUpdate();

            if (customerIdToBeDeleted != null) {
                log.info("Removing customer account with id=" + customerIdToBeDeleted.toString());
                sql = "delete from customer where id = ?";
                ps = con.prepareStatement(sql);
                ps.setInt(1, customerIdToBeDeleted.toInteger());
                ps.executeUpdate();
            } else {
                log.info("No customer entry for username " + userName);
            }

        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    /**
     * @return true if account has client role in account_role table
     */
    public boolean hasClientRole(UserName userName) {

        Connection con = null;
        String sql = "select count(*) from account_role where username like ? and role_name ='client'";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, userName.stringValue());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("%s failed with username: %s", sql, userName), e);
        }
    }

    public boolean accountReceiverExists(AccountId id, String orgNo) {

        Connection con = null;
        String sql = "select count(*) from account_receiver where account_id = ? and participant_id = ?";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id.toInteger());
            ps.setString(2, "9908:".concat(orgNo));


            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("%s failed with orgNo: %s", sql, orgNo), e);
        }
    }

    public JdbcTxManager getJdbcTxManager() {
        return jdbcTxManager;
    }

    public QueuedMessage getQueuedMessageByQueueId(OutboundMessageQueueId queueId) {
        Connection con = null;
        String sql = "select * from outbound_message_queue where id = ?";

        try {
            con = jdbcTxManager.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, queueId.toInt());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new QueuedMessage(rs.getInt("id"), rs.getLong("msg_no"), OutboundMessageQueueState.valueOf(rs.getString("state")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("error fetching fault messages", e);
        }
        return null;
    }

    public QueuedMessage getQueuedMessageByMsgNo(Long msgNo) {
        Connection con = null;
        String sql = "select * from outbound_message_queue where msg_no = ?";

        try {
            con = jdbcTxManager.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setLong(1, msgNo);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new QueuedMessage(rs.getInt("id"), rs.getLong("msg_no"), OutboundMessageQueueState.valueOf(rs.getString("state")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("error fetching fault messages", e);
        }
        return null;
    }

    public Integer putMessageOnQueue(Long msgId) {
        Connection con = null;
        String sql = "insert into outbound_message_queue (msg_no, state) values (?,?)";

        try {
            con = jdbcTxManager.getConnection();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, msgId);
            ps.setString(2, OutboundMessageQueueState.QUEUED.name());

            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new IllegalStateException("Unable to obtain generated key after insert.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    public List<QueuedOutboundMessageError> getErrorMessages() {
        List<QueuedOutboundMessageError> result = new ArrayList<QueuedOutboundMessageError>();

        Connection con = null;
        String sql = "select * from outbound_message_queue_error";

        try {
            con = jdbcTxManager.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new QueuedOutboundMessageError(new OutboundMessageQueueErrorId(rs.getInt("id")), new OutboundMessageQueueId(rs.getInt("queue_id")), null, rs.getString("message"), rs.getString("details"), rs.getString("stacktrace"), rs.getTimestamp("create_dt"), "1"));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("error fetching fault messages", e);
        }
    }

    public void updateValidateFlagOnAccount(AccountId accountId, boolean validateUpdate) {
        Connection con = null;
        String sql = "update account set validate_upload= ? where id = ?";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setBoolean(1, validateUpdate);
            ps.setInt(2, accountId.toInteger());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    public void removeExistingErrorMessages() {
        Connection con = null;
        String sql = "delete from outbound_message_queue_error";

        try {
            con = jdbcTxManager.getConnection();

            PreparedStatement ps = con.prepareStatement(sql);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(sql + " failed " + e, e);
        }
    }

    public class QueuedMessage {
        private final Integer queueId;
        private final Long msgNo;
        private final OutboundMessageQueueState status;

        public QueuedMessage(Integer queueId, Long msgNo, OutboundMessageQueueState status) {
            this.queueId = queueId;
            this.msgNo = msgNo;
            this.status = status;
        }

        public Long getMsgNo() {
            return msgNo;
        }

        public Integer getQueueId() {
            return queueId;
        }

        public OutboundMessageQueueState getState() {
            return status;
        }
    }

    public class FaultMessageRow {
        private final int messageNo;
        private final String message;
        private final Date ts;

        public FaultMessageRow(int messageNo, String message, Date ts) {
            this.messageNo = messageNo;
            this.message = message;
            this.ts = ts;
        }

        public int getMessageNo() {
            return messageNo;
        }

        public String getMessage() {
            return message;
        }

        public Date getTs() {
            return ts;
        }
    }
}
