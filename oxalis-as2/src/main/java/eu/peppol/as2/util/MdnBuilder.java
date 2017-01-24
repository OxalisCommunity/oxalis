package eu.peppol.as2.util;

import com.sun.mail.util.LineOutputStream;
import eu.peppol.util.OxalisVersion;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

public class MdnBuilder {

    private static final String ISSUER = String.format("Oxalis %s", OxalisVersion.getVersion());

    private InternetHeaders headers = new InternetHeaders();

    private ByteArrayOutputStream textOutputStream = new ByteArrayOutputStream();

    private LineOutputStream textLineOutputStream = new LineOutputStream(textOutputStream);

    public static MdnBuilder newInstance(MimeMessage mimeMessage) throws MessagingException, IOException {
        MdnBuilder mdnBuilder = new MdnBuilder();
        mdnBuilder.addHeader(MdnHeader.REPORTING_UA, ISSUER);

        String recipient = String.format("rfc822; %s", mimeMessage.getHeader(As2Header.AS2_TO)[0]);
        mdnBuilder.addHeader(MdnHeader.ORIGINAL_RECIPIENT, recipient);
        mdnBuilder.addHeader(MdnHeader.FINAL_RECIPIENT, recipient);

        mdnBuilder.textLineOutputStream.writeln("= Received headers");
        mdnBuilder.textLineOutputStream.writeln();
        for (Object header : Collections.list(mimeMessage.getAllHeaderLines()))
            mdnBuilder.textLineOutputStream.writeln((String) header);
        mdnBuilder.textLineOutputStream.writeln();

        return mdnBuilder;
    }

    private MdnBuilder() {
        // No action.
    }

    public void addHeader(String name, String value) {
        headers.addHeader(name, value);
    }

    public void addHeader(String name, Date value) {
        headers.addHeader(name, value.toString());
    }

    public void addHeader(String name, byte[] value) {
        headers.addHeader(name, Base64.getEncoder().encodeToString(value));
    }

    public void addHeader(String name, Object value) {
        headers.addHeader(name, value.toString());
    }

    public void build() throws MessagingException, IOException {
        // Initiate multipart
        MimeMultipart mimeMultipart = new MimeMultipart();

        // Insert text part
        MimeBodyPart textPart = new MimeBodyPart();
        textLineOutputStream.close();
        textPart.setContent(textOutputStream.toString(), "text/plain");
        mimeMultipart.addBodyPart(textPart);

        // Extract headers
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (LineOutputStream lineOutputStream = new LineOutputStream(outputStream)) {
            for (Object header : Collections.list(headers.getAllHeaderLines()))
                lineOutputStream.writeln((String) header);
        }

        // Insert header part
        MimeBodyPart headerPart = new MimeBodyPart();
        headerPart.setContent(outputStream.toString(), "message/disposition-notification");
        mimeMultipart.addBodyPart(headerPart);

        // Create MIME message
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        mimeMessage.setContent(mimeMultipart);

        // Dump to sysout
        mimeMessage.writeTo(System.out, new String[]{"Message-ID", "MIME-Version"});
    }
}
