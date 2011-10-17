package eu.peppol.outbound.client;

import eu.peppol.outbound.soap.SOAPHeaderObject;
import eu.peppol.outbound.util.Constants;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.util.Configuration;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * User: ravnholt
 */
public class TestStandAloneWSClient {

    public static void main(String[] args) throws Exception {
        //System.setProperty(
        //		"com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
        //		"true");
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");

        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        new TestStandAloneWSClient().testSend();
    }

    protected void testSend() throws Exception {
        Configuration configuration = Configuration.getInstance();
        String url = configuration.getProperty("web.service.address"); // Replace with SMP lookup

        Log.info("Calling webservice at: " + url);
        String xmlFile = configuration.getProperty("test.file");

        String senderScheme = "iso6523-actorid-upis";
        String senderValue = "9909:976098897";

        String receiverScheme = "iso6523-actorid-upis";
        String receiverValue = "9909:976098897"; // Kenneth Bengtsson og Alfa1lab: 9902:DK28158815

        String messageID = "uuid:" + UUID.randomUUID().toString();
        String channelID = "9909:976098897";

        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = parser.parse(new File(xmlFile));

        Create create = new Create();
        List<Object> objects = create.getAny();
        objects.add(document.getDocumentElement());

        SOAPHeaderObject soapHeaderObject = new SOAPHeaderObject();
        soapHeaderObject.setChannelIdentifier(channelID);
        soapHeaderObject.setMessageIdentifier(messageID);

        soapHeaderObject.setDocumentIdentifier(Constants.getInvoiceDocumentIdentifier());
        soapHeaderObject.setProcessIdentifier(Constants.getInvoiceProcessIdentifier());

        ParticipantIdentifierType senderIdentifierType = new ParticipantIdentifierType();
        senderIdentifierType.setValue(senderValue);
        senderIdentifierType.setScheme(senderScheme);
        soapHeaderObject.setSenderIdentifier(senderIdentifierType);

        ParticipantIdentifierType receiverIdentifierType = new ParticipantIdentifierType();
        receiverIdentifierType.setValue(receiverValue);
        receiverIdentifierType.setScheme(receiverScheme);
        soapHeaderObject.setRecipientIdentifier(receiverIdentifierType);

        accesspointClient accesspointClient = new accesspointClient();
        accesspointClient.enableSoapLogging(true);
        accesspointClient.send(url, soapHeaderObject, create);

        Log.info("Test message successfully dispatched");
    }
}
