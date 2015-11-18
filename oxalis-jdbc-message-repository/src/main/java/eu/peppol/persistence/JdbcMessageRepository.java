package eu.peppol.persistence;

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;
import eu.peppol.jdbc.OxalisDataSourceFactory;
import eu.peppol.jdbc.OxalisDataSourceFactoryProvider;
import eu.peppol.persistence.sql.util.DataSourceHelper;
import eu.peppol.util.OxalisVersion;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.sql.DataSource;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class JdbcMessageRepository implements MessageRepository {

    private static final Logger log = LoggerFactory.getLogger(SimpleMessageRepository.class);

    private DataSourceHelper dataSourceHelper;

    public JdbcMessageRepository() {
        OxalisDataSourceFactory oxalisDataSourceFactory = OxalisDataSourceFactoryProvider.getInstance();
        DataSource dataSource = oxalisDataSourceFactory.getDataSource();
        dataSourceHelper = new DataSourceHelper(dataSource);
    }

    @Override
    public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, Document document) throws OxalisMessagePersistenceException {
        log.info("Saving inbound message document using " + JdbcMessageRepository.class.getSimpleName());
        log.debug("Default inbound message headers " + peppolMessageMetaData);
        DOMSource source = new DOMSource(document);
        StringWriter xmlAsWriter = new StringWriter();
        StreamResult result = new StreamResult(xmlAsWriter);
        try {
            TransformerFactory.newInstance().newTransformer().transform(source, result);
            // write changes
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlAsWriter.toString().getBytes("UTF-8"));
            saveInboundMessage(peppolMessageMetaData, inputStream);
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Unable to execute statement " + e, e);
        } catch (TransformerException e) {
            throw new IllegalStateException("Unable to execute statement " + e, e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to execute statement " + e, e);
        }
    }

    @Override
    public void saveInboundMessage(PeppolMessageMetaData peppolMessageMetaData, InputStream inputStream) throws OxalisMessagePersistenceException {
        log.info("Saving inbound message stream using " + JdbcMessageRepository.class.getSimpleName());
        log.debug("Default inbound message headers " + peppolMessageMetaData);
        Connection con = null;
        PreparedStatement ps;
        try {
            String sqlStatement = "insert into oxa_messages (id, messageId, documentTypeIdentifier, profileTypeIdentifier, sendingAccessPoint, receivingAccessPoint, recipientId, recipientSchemeId, senderId, senderSchemeId, protocol, userAgent, userAgentVersion, sendersTimeStamp, receivedTimeStamp, sendingAccessPointPrincipal, transmissionId, buildUser, buildDescription, buildTimeStamp, oxalis, content) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            con = dataSourceHelper.getConnectionWithAutoCommit();
            ps = con.prepareStatement(sqlStatement);

            String messageId = null;
            if (peppolMessageMetaData.getMessageId() != null) {
                messageId = peppolMessageMetaData.getMessageId().stringValue();
            }

            String documentTypeIdentifier = null;
            if (peppolMessageMetaData.getDocumentTypeIdentifier() != null) {
                documentTypeIdentifier = peppolMessageMetaData.getDocumentTypeIdentifier().toString();
            }

            String profileTypeIdentifier = null;
            if (peppolMessageMetaData.getProfileTypeIdentifier() != null) {
                profileTypeIdentifier = peppolMessageMetaData.getProfileTypeIdentifier().toString();
            }

            String sendingAccessPoint = null;
            if (peppolMessageMetaData.getSendingAccessPoint() != null) {
                sendingAccessPoint = peppolMessageMetaData.getSendingAccessPoint().toString();
            }

            String receivingAccessPoint = null;
            if (peppolMessageMetaData.getReceivingAccessPoint() != null) {
                receivingAccessPoint = peppolMessageMetaData.getReceivingAccessPoint().toString();
            }

            String recipientId = null;
            String recipientSchemeId = null;
            if (peppolMessageMetaData.getRecipientId() != null) {
                recipientId = peppolMessageMetaData.getRecipientId().stringValue();
                recipientSchemeId = getSchemeId(peppolMessageMetaData.getRecipientId());
            }

            String senderId = null;
            String senderSchemeId = null;
            if (peppolMessageMetaData.getSenderId() != null) {
                senderId = peppolMessageMetaData.getSenderId().stringValue();
                senderSchemeId = getSchemeId(peppolMessageMetaData.getSenderId());
            }

            String protocol = null;
            if (peppolMessageMetaData.getProtocol() != null) {
                protocol = peppolMessageMetaData.getProtocol().toString();
            }

            Timestamp sendersTimeStamp = null;
            if (peppolMessageMetaData.getSendersTimeStamp() != null) {
                sendersTimeStamp = new Timestamp(peppolMessageMetaData.getSendersTimeStamp().getTime());
            }

            Timestamp receivedTimeStamp = null;
            if (peppolMessageMetaData.getReceivedTimeStamp() != null) {
                receivedTimeStamp = new Timestamp(peppolMessageMetaData.getReceivedTimeStamp().getTime());
            }

            String sendingAccessPointPrincipal = null;
            if (peppolMessageMetaData.getSendingAccessPointPrincipal() != null) {
                sendingAccessPointPrincipal = peppolMessageMetaData.getSendingAccessPointPrincipal().getName();
            }

            String transmissionId = null;
            if (peppolMessageMetaData.getTransmissionId() != null) {
                transmissionId = peppolMessageMetaData.getTransmissionId().toString();
            }

            byte[] content = null;
            if (inputStream != null) {
                content = IOUtils.toByteArray(inputStream);
            }

            ps.setString(1, peppolMessageMetaData.getId().toString());
            ps.setString(2, messageId);
            ps.setString(3, documentTypeIdentifier);
            ps.setString(4, profileTypeIdentifier);
            ps.setString(5, sendingAccessPoint);
            ps.setString(6, receivingAccessPoint);
            ps.setString(7, recipientId);
            ps.setString(8, recipientSchemeId);
            ps.setString(9, senderId);
            ps.setString(10, senderSchemeId);
            ps.setString(11, protocol);
            ps.setString(12, peppolMessageMetaData.getUserAgent());
            ps.setString(13, peppolMessageMetaData.getUserAgentVersion());
            ps.setTimestamp(14, sendersTimeStamp);
            ps.setTimestamp(15, receivedTimeStamp);
            ps.setString(16, sendingAccessPointPrincipal);
            ps.setString(17, transmissionId);
            ps.setString(18, OxalisVersion.getUser());
            ps.setString(19, OxalisVersion.getBuildDescription());
            ps.setString(20, OxalisVersion.getBuildTimeStamp());
            ps.setString(21, OxalisVersion.getVersion());
            ps.setBytes(22, content);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Unable to execute statement " + e, e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to execute statement " + e, e);
        } finally {
            DataSourceHelper.close(con);
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
}
