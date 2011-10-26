package no.sendregning.oxalis;

import eu.peppol.outbound.api.DocumentSender;
import eu.peppol.outbound.api.DocumentSenderBuilder;
import eu.peppol.start.util.Configuration;

import java.io.File;
import java.net.URL;

/**
 * User: ravnholt
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Configuration configuration = Configuration.getInstance();

        File xmlInvoice = new File(configuration.getProperty("test.file"));
        String recipient = "9909:976098897";
        URL destination = new URL(configuration.getProperty("web.service.address"));

        DocumentSender documentSender = new DocumentSenderBuilder()
                .setKeystoreFile(new File(configuration.getProperty("keystore")))
                .setKeystorePassword(configuration.getProperty("keystore.password"))
                .enableSoapLogging()
                .build();

        documentSender.sendInvoice(xmlInvoice, recipient, recipient, destination);

        Log.info("Test message successfully dispatched");
    }
}
