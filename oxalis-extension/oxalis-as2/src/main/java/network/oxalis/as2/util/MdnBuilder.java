/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package network.oxalis.as2.util;

import com.sun.mail.util.LineOutputStream;
import network.oxalis.as2.code.As2Header;
import network.oxalis.as2.code.Disposition;
import network.oxalis.as2.code.MdnHeader;
import network.oxalis.commons.util.OxalisVersion;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

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
        for (String header : Collections.list((Enumeration<String>) mimeMessage.getAllHeaderLines()))
            mdnBuilder.textLineOutputStream.writeln(header);
        mdnBuilder.textLineOutputStream.writeln();

        return mdnBuilder;
    }

    private MdnBuilder() {
        // No action.
    }

    public void addText(String title, String text) throws IOException {
        textLineOutputStream.writeln(String.format("= %s", title));
        textLineOutputStream.writeln();
        textLineOutputStream.writeln(String.valueOf(text));
        textLineOutputStream.writeln();
    }

    public void addHeader(String name, String value) {
        headers.addHeader(name, value);
    }

    public void addHeader(String name, Date value) {
        headers.addHeader(name, As2DateUtil.RFC822.format(value));
    }

    public void addHeader(String name, byte[] value) {
        headers.addHeader(name, Base64.getEncoder().encodeToString(value));
    }

    public void addHeader(String name, Object value) {
        headers.addHeader(name, value.toString());
    }

    public void addHeader(String name, Disposition disposition) {
        headers.addHeader(name, disposition.toString());
    }

    public MimeBodyPart build() throws MessagingException, IOException {
        // Initiate multipart
        MimeMultipart mimeMultipart = new MimeMultipart();
        mimeMultipart.setSubType("report; Report-Type=disposition-notification");

        // Insert text part
        MimeBodyPart textPart = new MimeBodyPart();
        textLineOutputStream.close();
        textPart.setContent(textOutputStream.toString("UTF-8"), "text/plain");
        // textPart.setHeader("Content-Type", "text/plain");
        mimeMultipart.addBodyPart(textPart);

        // Extract headers
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LineOutputStream lineOutputStream = new LineOutputStream(outputStream);
        for (String header : Collections.list((Enumeration<String>) headers.getAllHeaderLines()))
            lineOutputStream.writeln(header);
        lineOutputStream.close();

        // Insert header part
        MimeBodyPart headerPart = new MimeBodyPart();
        headerPart.setContent(outputStream.toString(), "message/disposition-notification");
        mimeMultipart.addBodyPart(headerPart);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(mimeMultipart, mimeMultipart.getContentType());
        return mimeBodyPart;
    }
}
