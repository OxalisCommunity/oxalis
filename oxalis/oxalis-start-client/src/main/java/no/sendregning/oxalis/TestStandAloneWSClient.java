package no.sendregning.oxalis;

import eu.peppol.outbound.client.DocumentSender;
import eu.peppol.outbound.util.Constants;
import eu.peppol.outbound.util.Log;
import eu.peppol.start.util.Configuration;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;

import java.io.File;
import java.net.URL;

/**
 * User: nigel
 * Date: Oct 18, 2011
 * Time: 8:45:15 AM
 */
public class TestStandAloneWSClient {

    public static void main(String[] args) throws Exception {
        System.setProperty("com.sun.xml.ws.client.ContentNegotiation", "none");
        System.setProperty("com.sun.xml.wss.debug", "FaultDetail");

        //System.setProperty("user.dir", "/Users/nigel/Filer/mazeppa/SendRegning/sr-peppol/oxalis/oxalis-start-client");

        //new TestStandAloneWSClient().testSend();
        //new TestDaemon().run();
        new TestStandAloneWSClient().testSend();
    }

    protected void testSend() throws Exception {
        Configuration configuration = Configuration.getInstance();

        File xmlInvoice = new File(configuration.getProperty("test.file"));
        ParticipantIdentifierType recipient = Constants.getParticipantIdentifier("9909:976098897");
        URL destination = new URL(configuration.getProperty("web.service.address"));

        new DocumentSender().sendInvoice(xmlInvoice, recipient, destination);

        Log.info("Test message successfully dispatched");
    }
}

