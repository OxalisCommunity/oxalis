/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.as2;

import org.testng.annotations.Test;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 13.11.13
 *         Time: 10:30
 */
@Test
public class MdnMimeMessageInspectorTest {

    public static final String OPENAS2_MDN_TXT = "openas2-mdn.txt";
    public static final String OPENAS2_MDN_NO_HEADERS_TXT = "openas2-mdn-no-headers.txt";

    @Test
    public void parseOpenAS2MDN() throws Exception {

        InputStream resourceAsStream = MdnMimeMessageInspectorTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_TXT);
        assertNotNull(resourceAsStream, "Unable to find " + OPENAS2_MDN_TXT + " in class path");

        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessage(resourceAsStream);
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
        String plainText = mdnMimeMessageInspector.getPlainTextPartAsText();
        assertNotNull(plainText);

    }

    @Test
    public void parseOpenAS2MDNWithoutHeaders() throws MimeTypeParseException, MessagingException, IOException {

        InputStream resourceAsStream = MdnMimeMessageInspectorTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_NO_HEADERS_TXT);
        assertNotNull(resourceAsStream, "Unable to find " + OPENAS2_MDN_NO_HEADERS_TXT + " in class path");

        MimeMessage mimeMessage = MimeMessageHelper.parseMultipart(resourceAsStream,
                new MimeType("multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha1;" +
                "\tboundary=\"----=_Part_2_1193010873.1384331414156\""));
        MimeMultipart mimeMultipart = (MimeMultipart) mimeMessage.getContent();
        assertEquals(new MimeType(mimeMultipart.getContentType()).getBaseType(), new MimeType("multipart/signed").getBaseType());

        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
        String plainText = mdnMimeMessageInspector.getPlainTextPartAsText();
        assertNotNull(plainText);

    }

    @Test
    public void parseOpenAS2MDNFields() throws Exception {

        InputStream resourceAsStream = MdnMimeMessageInspectorTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_TXT);
        assertNotNull(resourceAsStream, "Unable to find " + OPENAS2_MDN_TXT + " in class path");

        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessage(resourceAsStream);
        MdnMimeMessageInspector mdnMimeMessageInspector = new MdnMimeMessageInspector(mimeMessage);
        Map<String, String> fields = mdnMimeMessageInspector.getMdnFields();
        assertEquals(fields.get("Original-Recipient"), "rfc822; OpenAS2A");
        assertEquals(fields.get("Final-Recipient"), "rfc822; OpenAS2A");
        assertEquals(fields.get("Original-Message-ID"), "42");
        assertEquals(fields.get("Received-Content-MIC"), "Fp67Ews9SJa5pKGXVl07dBuVW4I=, sha1");

    }

}
