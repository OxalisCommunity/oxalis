package eu.peppol.outbound.client;

import eu.peppol.outbound.client.accesspointClient;
import eu.peppol.outbound.soap.SOAPHeaderObject;
import eu.peppol.outbound.soap.handler.SOAPOutboundHandler;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.util.Configuration;
import eu.peppol.start.util.Daemon;
import eu.peppol.start.util.Time;

import org.w3._2009._02.ws_tra.AccesspointService;
import org.w3._2009._02.ws_tra.Create;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.FaultMessage;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;
import org.w3._2009._02.ws_tra.Resource;
import org.w3c.dom.Document;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User: ravnholt
 */
public class TestStandAloneWSClient {

	public static void main(String[] args) throws Exception {
		System.setProperty(
				"com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
				"true");
		System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");

		System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

		new TestStandAloneWSClient().testSend();
	}

	protected void testSend() throws Exception {
		Configuration configuration = Configuration.getInstance();
		String url = configuration.getProperty("web.service.address"); // Replace with SMP lookup

		Log.info("Calling webservice at: " + url);
		String xmlFile = configuration.getProperty("test.file");

		String documentScheme = "busdox-docid-qns";
		String documentValue = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0";

		String processScheme = "cenbii-procid-ubl";
		String processValue = "urn:www.cenbii.eu:profile:bii04:ver1.0";

		String senderScheme = "iso6523-actorid-upis";
		String senderValue = "9909:976098897";

		String receiverScheme = "iso6523-actorid-upis";
		String receiverValue = "9902:DK28158815";

		String messageID = "uuid:" + UUID.randomUUID().toString();
		String channelID = "9909:976098897";

		DocumentBuilder parser = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document document = parser.parse(new File(xmlFile));

		Create create = new Create();
		List<Object> objects = create.getAny();
		objects.add(document.getDocumentElement());

		SOAPHeaderObject soapHeaderObject = new SOAPHeaderObject();
		soapHeaderObject.setChannelIdentifier(channelID);
		soapHeaderObject.setMessageIdentifier(messageID);

		DocumentIdentifierType documentIdentifierType = new DocumentIdentifierType();
		documentIdentifierType.setValue(documentValue);
		documentIdentifierType.setScheme(documentScheme);
		soapHeaderObject.setDocumentIdentifier(documentIdentifierType);

		ProcessIdentifierType processIdentifierType = new ProcessIdentifierType();
		processIdentifierType.setValue(processValue);
		processIdentifierType.setScheme(processScheme);
		soapHeaderObject.setProcessIdentifier(processIdentifierType);

		ParticipantIdentifierType senderIdentifierType = new ParticipantIdentifierType();
		senderIdentifierType.setValue(senderValue);
		senderIdentifierType.setScheme(senderScheme);
		soapHeaderObject.setSenderIdentifier(senderIdentifierType);

		ParticipantIdentifierType receiverIdentifierType = new ParticipantIdentifierType();
		receiverIdentifierType.setValue(receiverValue);
		receiverIdentifierType.setScheme(receiverScheme);
		soapHeaderObject.setRecipientIdentifier(receiverIdentifierType);


		accesspointClient accesspointClient = new accesspointClient();
		accesspointClient.printSOAPLogging(true);
		accesspointClient.send(accesspointClient.getPort(url),soapHeaderObject, create);

		Log.info("Test message successfully dispatched");
	}
}
