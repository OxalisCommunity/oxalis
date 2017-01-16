package eu.peppol.persistence.jdbc;

import eu.peppol.PeppolTransmissionMetaData;
import eu.peppol.evidence.TransmissionEvidence;
import eu.peppol.identifier.*;
import eu.peppol.persistence.*;
import eu.peppol.persistence.file.ArtifactType;
import eu.peppol.persistence.guice.TestModuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.*;
import java.util.Date;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.fail;

/**
 * @author Steinar Overbeck Cook
 * @author Thore Holmberg Johnsen
 */
@Guice(moduleFactory = TestModuleFactory.class)
public class MessageRepositoryH2ImplTest {

    public static final Logger log = LoggerFactory.getLogger(MessageRepositoryH2ImplTest.class);
    private static final String MESSAGE_COMMENT = "This is a simple JUnit test";

    @Inject
    private MessageRepository messageDbmsRepository;

    @Inject
    private DataSource dataSource;

    @Test
    public void findAccountByParticipantId() {
        MessageRepositoryH2Impl repo = (MessageRepositoryH2Impl) messageDbmsRepository;
        AccountId accountId = repo.srAccountIdForReceiver(new ParticipantId("9908:976098897"));
        assertEquals(accountId.toInteger(), Integer.valueOf(1));
    }


    @Test
    public void testSaveInboundMessage() throws Exception {
        PeppolTransmissionMetaData PeppolTransmissionMetaData = sampleMessageHeader();
        Long messageNo = messageDbmsRepository.saveInboundMessage(PeppolTransmissionMetaData, sampeXmlDocumentAsInputStream());
        MessageMetaData metaData = messageDbmsRepository.findByMessageNo(messageNo);

        removeFilesFor(metaData);
    }

    void removeFilesFor(MessageMetaData metaData) {

        deleteUri(metaData.getPayloadUri());
        if (metaData.getNativeEvidenceUri() != null) {
            deleteUri(metaData.getNativeEvidenceUri());
        }
    }

    private void deleteUri(URI p) {
        try {
            Files.deleteIfExists(Paths.get(p));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to remove " + p);
        }
    }


    @Test
    public void testSaveInboundMessageWithArtifactsInFileStore() throws ParserConfigurationException, SQLException {

        PeppolTransmissionMetaData peppolTransmissionMetaData = sampleMessageHeader();
        TransmissionEvidence transmissionEvidence = new TransmissionEvidence() {
            @Override
            public Date getReceptionTimeStamp() {
                return new Date();
            }

            @Override
            public InputStream getNativeEvidenceStream() {
                return new ByteArrayInputStream("Smime rubbish".getBytes());
            }
        };

        Long messageNo = null;
        try {
            messageNo = messageDbmsRepository.saveInboundMessage(peppolTransmissionMetaData, sampeXmlDocumentAsInputStream());
            assertNotNull(messageNo);

            messageDbmsRepository.saveInboundTransportReceipt(transmissionEvidence, peppolTransmissionMetaData);

        } catch (OxalisMessagePersistenceException e) {
            fail(e.getMessage());
        }

        // Verifies the contents of the database and directories

        MessageMetaData metaData = messageDbmsRepository.findByMessageNo(messageNo);


        Long msg_no = metaData.getMessageNumber().toLong();
        assertNotNull(msg_no);

        // Verifies the path and existence of the message payload, the native receipt and the transport receipt
        String payloadUrl = metaData.getPayloadUri().toString();
        assertNotNull(payloadUrl, "No payload url found");
        assertTrue(payloadUrl.endsWith(ArtifactType.PAYLOAD.getFileNameSuffix()), "Seems the payload suffix is wrong");

        assertNotNull(metaData.getNativeEvidenceUri(), "Column " + ArtifactType.NATIVE_EVIDENCE.getColumnName() + " is null in DBMS");
        assertTrue(metaData.getNativeEvidenceUri().toString().endsWith(ArtifactType.NATIVE_EVIDENCE.getFileNameSuffix()), "Invalid suffix for native evidence url");

        // Removes the file artifacts
        removeFilesFor(metaData);
    }


    private void dumpRow(ResultSet resultSet) throws SQLException {

        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnLabel = metaData.getColumnLabel(i);
            String columnName = metaData.getColumnName(i);
            String value = resultSet.getString(i);
            System.out.format("%s (%s): %s\n", columnName, columnName, value == null ? "null" : value);

        }
    }

    /**
     * The Participant ID does not exist in the database, and will thus cause the account_id to be null
     * when persisting the message
     */
    @Test
    public void testSaveInboundMessageWithoutAccountId() throws Exception {
        PeppolTransmissionMetaData PeppolTransmissionMetaData = sampleMessageHeader();
        PeppolTransmissionMetaData.setRecipientId(new ParticipantId("9908:917686688"));
        messageDbmsRepository.saveInboundMessage(PeppolTransmissionMetaData, sampeXmlDocumentAsInputStream());
    }

    /**
     * Sending a message without the participant id of the sender is absolutely illegal.
     *
     * @throws ParserConfigurationException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSaveInboundMessageWithoutSenderNull() throws ParserConfigurationException, OxalisMessagePersistenceException {
        PeppolTransmissionMetaData h = sampleMessageHeader();
        h.setSenderId(null);
        messageDbmsRepository.saveInboundMessage(h, sampeXmlDocumentAsInputStream());
    }

    /**
     * Sending a message without specifying the Participant ID of the receiver is an error.
     *
     * @throws ParserConfigurationException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSaveInboundMessageWithoutRecipientIdNull() throws ParserConfigurationException, OxalisMessagePersistenceException {
        PeppolTransmissionMetaData h = sampleMessageHeader();
        h.setRecipientId(null);
        messageDbmsRepository.saveInboundMessage(h, sampeXmlDocumentAsInputStream());
    }

    @Test
    public void testSaveInboundMessageWithoutProcessId() throws ParserConfigurationException, OxalisMessagePersistenceException {
        PeppolTransmissionMetaData h = sampleMessageHeader();
        h.setProfileTypeIdentifier(null);
        messageDbmsRepository.saveInboundMessage(h, sampeXmlDocumentAsInputStream());
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSaveInboundMessageWithoutDocumentId() throws ParserConfigurationException, OxalisMessagePersistenceException {
        PeppolTransmissionMetaData h = sampleMessageHeader();
        h.setDocumentTypeIdentifier(null);
        messageDbmsRepository.saveInboundMessage(h, sampeXmlDocumentAsInputStream());
        fail("Expected exception to be thrown here");
    }

    @Test
    public void testSaveOutboundMessage() throws ParserConfigurationException, SQLException, IOException, OxalisMessagePersistenceException {

        Long messageNo = messageDbmsRepository.saveOutboundMessage(sampleMessageMetaData(), sampeXmlDocumentAsInputStream());

        Connection con = dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement("select * from message where msg_no=?");
        ps.setLong(1, messageNo);
        ResultSet resultSet = ps.executeQuery();
        assertTrue(resultSet.next());

        String sender = resultSet.getString("SENDER");
        assertNotNull(sender);
        assertEquals(sender, WellKnownParticipant.DIFI.stringValue());


        String receiver = resultSet.getString("RECEIVER");
        assertNotNull(receiver);
        assertEquals(receiver, WellKnownParticipant.DIFI_TEST.stringValue());

        assertNotNull(resultSet.getString("CHANNEL"));
        assertNotNull(resultSet.getString("MESSAGE_UUID"));

        String payloadUrl = resultSet.getString("PAYLOAD_URL");
        assertNotNull(payloadUrl);
        byte[] bytes = Files.readAllBytes(Paths.get(URI.create(payloadUrl)));
        String s = new String(bytes, Charset.forName("UTF-8"));
        assertTrue(s.contains(MESSAGE_COMMENT), "Oops, seems that " + payloadUrl + " does not contain the expected data!");

        // dumpRow(resultSet);
    }

    private MessageMetaData sampleMessageMetaData() {
        MessageMetaData.Builder builder = new MessageMetaData.Builder(TransferDirection.OUT, WellKnownParticipant.DIFI, WellKnownParticipant.DIFI_TEST, PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier(), ChannelProtocol.SREST);
        MessageMetaData messageMetaData = builder.accountId(1)
                .build();
        return messageMetaData;
    }

    @Test
    public void testTransformDocumentWithLSSerializer() throws Exception {
        byte[] bytes = transformDocumentWithLSSerializer(sampleXmlDocument());
        String result = new String(bytes);
        assertEquals(result, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<sr-invoice><!--This is a simple JUnit test--><person>Steinar O. Cook</person></sr-invoice>");
    }

    private Document sampleXmlDocument() throws ParserConfigurationException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document sampleDocument = documentBuilder.newDocument();
        Element root = sampleDocument.createElement("sr-invoice");
        sampleDocument.appendChild(root);
        Comment comment = sampleDocument.createComment(MESSAGE_COMMENT);
        root.appendChild(comment);
        Element person = sampleDocument.createElement("person");
        root.appendChild(person);
        person.appendChild(sampleDocument.createTextNode("Steinar O. Cook"));
        return sampleDocument;
    }

    private InputStream sampeXmlDocumentAsInputStream() throws ParserConfigurationException {
        return new ByteArrayInputStream(transformDocumentWithLSSerializer(sampleXmlDocument()));
    }

    private PeppolTransmissionMetaData sampleMessageHeader() {
        PeppolTransmissionMetaData PeppolTransmissionMetaData = new PeppolTransmissionMetaData();
        PeppolTransmissionMetaData.setDocumentTypeIdentifier(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier());
        PeppolTransmissionMetaData.setMessageId(new MessageId());
        PeppolTransmissionMetaData.setProfileTypeIdentifier(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId());
        PeppolTransmissionMetaData.setRecipientId(new ParticipantId("9908:976098897"));
        PeppolTransmissionMetaData.setSenderId(new ParticipantId("9908:976098897"));
        PeppolTransmissionMetaData.setSendingAccessPoint(new AccessPointIdentifier("AP_TEST"));
        PeppolTransmissionMetaData.setSendingAccessPointPrincipal(new Principal() {
            @Override
            public String getName() {
                return "CN=APP_1000000001, O=SendRegning, C=NO";
            }
        });
        return PeppolTransmissionMetaData;
    }

    private byte[] transformDocumentWithLSSerializer(Document document) {
        DOMImplementationLS domImplementationLS = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImplementationLS.createLSSerializer();
        LSOutput lsOutput = domImplementationLS.createLSOutput();
        lsOutput.setEncoding("UTF-8");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        lsOutput.setByteStream(stream);
        serializer.write(document, lsOutput);
        return stream.toByteArray();
    }
}
