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
import javax.mail.BodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.InputStream;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.*;

/**
 * @author steinar
 *         Date: 13.11.13
 *         Time: 11:12
 */
public class MimeMessageHelperTest {

    public static final String OPENAS2_MDN_TXT = "openas2-mdn.txt";
    public static final String OPENAS2_MDN_NO_HEADERS_TXT = "openas2-mdn-no-headers.txt";

    @Test
    public void parseLegalMimeMessageWithHeaders() throws Exception {
        InputStream resourceAsStream = MimeMessageHelperTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_TXT);
        assertNotNull(resourceAsStream, OPENAS2_MDN_TXT + " not found in classpath");

        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessage(resourceAsStream);
        Object content = mimeMessage.getContent();
        assertNotNull(content);
        String contentType = mimeMessage.getContentType();
        assertEquals(new MimeType(contentType).getBaseType(), new MimeType("multipart/signed").getBaseType());

        MimeMultipart mimeMultipartSigned = (MimeMultipart) content;

        // Two Bodies in the MimeMultiPart
        assertEquals(mimeMultipartSigned.getCount(), 2);

        // First body is a multipart/report
        BodyPart firstBody = mimeMultipartSigned.getBodyPart(0);
        assertEquals(new MimeType(firstBody.getContentType()).getBaseType(), new MimeType("multipart/report").getBaseType());

        // Second body contains the signature
        BodyPart secondBody = mimeMultipartSigned.getBodyPart(1);
        assertEquals(new MimeType(secondBody.getContentType()).getBaseType(), new MimeType("application/pkcs7-signature").getBaseType());

        // The inner multipart should contain two bodies: the text/plain and the message/disposition-notification
        MimeMultipart innerMultiPart = (MimeMultipart) firstBody.getContent();
        assertNotNull(innerMultiPart);
        // Must have two parts
        assertEquals(innerMultiPart.getCount(), 2);

        // First part is text/plain
        assertEquals(new MimeType(innerMultiPart.getBodyPart(0).getContentType()).getBaseType(), new MimeType("text/plain").getBaseType());

        // Second part of first part in multipart is message/disposition-notification
        assertEquals(new MimeType(innerMultiPart.getBodyPart(1).getContentType()).getBaseType(), new MimeType("message/disposition-notification").getBaseType());

    }

    /**
     * Verifies that if you don't supply the headers, a plain/text message will be created even though it might appear as being a multipart.
     *
     * @throws Exception
     */
    @Test
    public void parseMimeMessageStreamWithoutContentTypeEmbedded() throws Exception {
        InputStream resourceAsStream = MimeMessageHelperTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_NO_HEADERS_TXT);
        assertNotNull(resourceAsStream, OPENAS2_MDN_NO_HEADERS_TXT + " not found in classpath");

        MimeMessage mimeMessage = MimeMessageHelper.createMimeMessage(resourceAsStream);
        Object content = mimeMessage.getContent();
        assertNotNull(content);
        assertTrue(content instanceof String);
        assertEquals(mimeMessage.getContentType(), "text/plain");
    }

    /**
     * Verifies that if you supply the correct "Content-Type:" together with an input stream, which does not contain the
     * required "Content-Type:" header at the start, may be created by simply supplying the header.
     *
     * This would mimic how to create a mime message from a Servlet input stream.
     *
     * @throws Exception
     */
    @Test
    public void parseMimeMessageStreamWithSuppliedContentType() throws Exception {

        InputStream resourceAsStream = MimeMessageHelperTest.class.getClassLoader().getResourceAsStream(OPENAS2_MDN_NO_HEADERS_TXT);
        assertNotNull(resourceAsStream, OPENAS2_MDN_NO_HEADERS_TXT + " not found in classpath");

        MimeType mimeType = new MimeType("multipart/signed; protocol=\"application/pkcs7-signature\"; micalg=sha1;\n" +
                "\tboundary=\"----=_Part_2_1193010873.1384331414156\"");
        assertEquals(mimeType.getBaseType(), "multipart/signed");
        assertEquals(mimeType.getSubType(), "signed");

        MimeMessage m2 = MimeMessageHelper.parseMultipart(resourceAsStream, mimeType);

        m2.writeTo(System.out);
        Object content2 = m2.getContent();
        assertTrue(content2 instanceof MimeMultipart, "Not MimeMultiPart as excpected, but " + content2.getClass().getSimpleName());
        assertEquals(new MimeType(m2.getContentType()).getBaseType(), new MimeType("multipart/signed").getBaseType());
    }
}
