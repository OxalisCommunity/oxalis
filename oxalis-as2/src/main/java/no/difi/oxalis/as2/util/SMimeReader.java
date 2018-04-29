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

package no.difi.oxalis.as2.util;

import com.google.common.io.ByteStreams;
import com.sun.mail.util.LineOutputStream;
import no.difi.oxalis.as2.code.As2Header;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * S/MIME helper.
 *
 * @author erlend
 */
public class SMimeReader implements Closeable {

    private MimeMultipart mimeMultipart;

    private byte[] signature;

    private SMimeDigestMethod sMimeDigestMethod;

    public SMimeReader(MimeMessage mimeMessage) throws MessagingException, IOException {
        this.mimeMultipart = (MimeMultipart) mimeMessage.getContent();

        // Extracting signature
        signature = ByteStreams.toByteArray(((InputStream) mimeMultipart.getBodyPart(1).getContent()));

        // Extracting DNO
        String[] dno = mimeMessage.getHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS);

        // if (dno == null)
        // throw new IllegalStateException("Unable to extract dno.");

        String algorithm = new ContentType(mimeMessage.getContentType()).getParameter("micalg");
        if (algorithm == null) {
            throw new MessagingException("micalg parameter not found in Content-Type header: " + mimeMessage.getContentType());
        }
        sMimeDigestMethod = SMimeDigestMethod.findByIdentifier(algorithm);
    }

    /**
     * Extracts headers of body MIME part. Creates headers as done by Bouncycastle.
     *
     * @return Headers
     */
    public byte[] getBodyHeader() throws MessagingException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LineOutputStream los = new LineOutputStream(outputStream);

        Enumeration hdrLines = ((MimeBodyPart) mimeMultipart.getBodyPart(0)).getNonMatchingHeaderLines(new String[]{});
        while (hdrLines.hasMoreElements())
            los.writeln((String) hdrLines.nextElement());

        // The CRLF separator between header and content
        los.writeln();
        los.close();

        return outputStream.toByteArray();
    }

    /**
     * Extracts content in body MIME part.
     *
     * @return Content
     */
    public InputStream getBodyInputStream() throws MessagingException, IOException {
        return mimeMultipart.getBodyPart(0).getInputStream();
    }

    public SMimeDigestMethod getDigestMethod() {
        return sMimeDigestMethod;
    }

    /**
     * Extracts signature in signature MIME part.
     *
     * @return Signature
     */
    public byte[] getSignature() throws MessagingException, IOException {
        return signature;
    }

    @Override
    public void close() throws IOException {
        mimeMultipart = null;
        signature = null;
    }
}
