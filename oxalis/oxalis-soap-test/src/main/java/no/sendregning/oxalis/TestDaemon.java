package no.sendregning.oxalis;

import eu.peppol.outbound.client.PeppolDocumentSender;
import eu.peppol.outbound.util.Constants;
import eu.peppol.start.util.Configuration;
import eu.peppol.start.util.Daemon;
import eu.peppol.start.util.Time;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;

import java.io.File;
import java.net.URL;

/**
 * User: nigel Date: Oct 8, 2011 Time: 9:29:08 AM
 */
public class TestDaemon extends Daemon {

    protected void init() {
        setInitialDelay(new Time(1, Time.SECONDS));
        setAntallIterasjoner(1);
    }

    protected void run() throws Exception {
        Configuration configuration = Configuration.getInstance();

        File xmlInvoice = new File(configuration.getProperty("test.file"));
        ParticipantIdentifierType recipient = Constants.getParticipantIdentifier("9909:976098897");
        URL destination = new URL(configuration.getProperty("web.service.address"));

        new PeppolDocumentSender().sendInvoice(xmlInvoice, recipient, destination);

        Log.info("Test message successfully dispatched");
    }
}
