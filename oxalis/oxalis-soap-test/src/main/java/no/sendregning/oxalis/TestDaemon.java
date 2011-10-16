package no.sendregning.oxalis;

import eu.peppol.outbound.client.accesspointClient;
import eu.peppol.outbound.soap.SOAPHeaderObject;
import eu.peppol.outbound.util.Constants;
import eu.peppol.start.util.Configuration;
import eu.peppol.start.util.Daemon;
import eu.peppol.start.util.Time;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * User: nigel Date: Oct 8, 2011 Time: 9:29:08 AM
 */
public class TestDaemon extends Daemon {

	public static void main(String[] args) throws Exception {
		System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");

		new TestDaemon().run();
	}

	protected void init() {
		setInitialDelay(new Time(1, Time.SECONDS));
		setAntallIterasjoner(1);
	}

	protected void run() throws Exception {
		Configuration configuration = Configuration.getInstance();
		String url = configuration.getProperty("web.service.address");
		String xmlFile = configuration.getProperty("test.file");

		String senderScheme = "iso6523-actorid-upis";
		String senderValue = "9909:976098897";

		String receiverScheme = "iso6523-actorid-upis";
		String receiverValue = "9909:976098897";

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
