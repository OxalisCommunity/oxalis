package eu.peppol.start.persistence;

import eu.peppol.start.util.IdentifierName;
import eu.peppol.start.util.Log;
import org.w3c.dom.Document;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 * MessageRepository implementation which will store the supplied messages and meta data into a MySQL database.
 *
 * @author Steinar Overbeck Cook
 *         Created by
 *         User: steinar
 *         Date: 28.11.11
 *         Time: 21:42
 */
public class MessageDbmsRepository implements MessageRepository {

    protected static final String INSERT_INTO_MESSAGE_SQL = "insert into oxalis.message (sender, receiver, channel, message_id, document_id, process_id, message) values(?,?,?,?,?,?,?)";

    public MessageDbmsRepository(){
        Log.debug("Initializing the MessageDBMS persistence....");
    }

    @Override
    public void saveMessage(Map<IdentifierName, String> properties, Document document) {

        Connection connection = null;
        try {
            Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource dataSource = (DataSource) envCtx.lookup("jdbc/peppol-ap");
            connection = dataSource.getConnection();
            PreparedStatement insertStatement = connection.prepareStatement(INSERT_INTO_MESSAGE_SQL);
            insertStatement.setString(1, properties.get(IdentifierName.SENDER_ID));
            insertStatement.setString(2, properties.get(IdentifierName.RECIPIENT_ID));
            insertStatement.setString(3, properties.get(IdentifierName.CHANNEL_ID));
            insertStatement.setString(4, properties.get(IdentifierName.MESSAGE_ID));
            insertStatement.setString(5, properties.get(IdentifierName.DOCUMENT_ID));
            insertStatement.setString(6, properties.get(IdentifierName.PROCESS_ID));

            String documentAsString = transformDocument(document);
            insertStatement.setString(7, documentAsString);
            
            insertStatement.executeUpdate();
            insertStatement.close();
            connection.close();

            Log.debug("Inserted message into oxalis.message");

        } catch (NamingException ne) {
            Log.error("Unable to create initial JNDI context. " + ne,ne);
            Log.error("You need to inspect your JNDI configuration in your web application environment, i.e. the context.xml and server.xml in Tomcat.");
        } catch (SQLException e) {
            Log.error("Unable to insert into message table using " + INSERT_INTO_MESSAGE_SQL + ", " + e, e);
            Log.error("Please ensure that the DBMS and the table is available according to the JNDI configuration.");
        }
    }

    /**
     * Transforms an XML document into a String
     * @param document the XML document to be transformed
     * @return the string holding the XML document
     */
    private String transformDocument(Document document) {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.transform(new DOMSource(document), result);
            
            return writer.toString();
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Unable to create an XML transformer");
        } catch (TransformerException e) {
            throw new IllegalStateException("Unable to transform XML document into a string");
        }
        
    }
}
