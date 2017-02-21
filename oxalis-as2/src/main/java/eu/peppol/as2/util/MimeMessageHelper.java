/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package eu.peppol.as2.util;

import com.google.common.io.ByteStreams;
import eu.peppol.as2.model.Mic;
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Collection of useful methods for manipulating MIME messages.
 *
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 * @author Arun Kumar
 */
public class MimeMessageHelper {

    private static Base64.Encoder encoder = Base64.getEncoder();

    public static final Logger log = LoggerFactory.getLogger(MimeMessageHelper.class);

    /**
     * Creates a MIME message from the supplied stream, which <em>must</em> contain headers,
     * especially the header "Content-Type:"
     */
    public static MimeMessage createMimeMessage(InputStream inputStream) {
        try {
            Properties properties = System.getProperties();
            Session session = Session.getDefaultInstance(properties, null);
            return new MimeMessage(session, inputStream);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create MimeMessage from input stream. " + e.getMessage(), e);
        }
    }

    /**
     * Creates a MimeMultipart MIME message from an input stream, which does not contain the header "Content-Type:".
     * Thus the mime type must be supplied as an argument.
     */
    public static MimeMessage parseMultipart(InputStream contents, String mimeType) {
        try {
            ByteArrayDataSource dataSource = new ByteArrayDataSource(contents, mimeType);
            return multipartMimeMessage(dataSource);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create ByteArrayDataSource; " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create Multipart mime message; " + e.getMessage(), e);
        }
    }

    /**
     * Creates a MIME message from the supplied InputStream, using values from the HTTP headers to
     * do a successful MIME decoding.  If MimeType can not be extracted from the HTTP headers we
     * still try to do a successful decoding using the payload directly.
     */
    public static MimeMessage createMimeMessageAssistedByHeaders(InputStream inputStream, InternetHeaders headers)
            throws MessagingException {
        String mimeType = null;
        String contentType = headers.getHeader("Content-Type", ",");
        if (contentType != null) {
            // From rfc2616 :
            // Multiple message-header fields with the same field-name MAY be present in a message if and only
            // if the entire field-value for that header field is defined as a comma-separated list.
            // It MUST be possible to combine the multiple header fields into one "field-name: field-value" pair,
            // without changing the semantics of the message, by appending each subsequent field-value to the first,
            // each separated by a comma.
            mimeType = contentType;
        }

        MimeMessage mimeMessage;
        if (mimeType == null) {
            log.warn("Headers did not contain MIME content type, trying to decode content type from body.");
            mimeMessage = MimeMessageHelper.parseMultipart(inputStream);
        } else {
            mimeMessage = MimeMessageHelper.parseMultipart(inputStream, mimeType);
        }

        for (Header header : (List<Header>) Collections.list(headers.getAllHeaders()))
            mimeMessage.addHeader(header.getName(), header.getValue());

        return mimeMessage;
    }


    public static MimeMessage parseMultipart(InputStream inputStream) {
        try {
            return new MimeMessage(Session.getDefaultInstance(System.getProperties()), inputStream);
        } catch (MessagingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static MimeMessage parseMultipart(String contents) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(contents.getBytes());
        return parseMultipart(byteArrayInputStream);
    }

    public static MimeMessage multipartMimeMessage(ByteArrayDataSource dataSource) throws MessagingException {
        MimeMultipart mimeMultipart = new MimeMultipart(dataSource);
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        mimeMessage.setContent(mimeMultipart);
        return mimeMessage;
    }

    public static MimeBodyPart createMimeBodyPart(InputStream inputStream, String mimeType) {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        ByteArrayDataSource byteArrayDataSource;

        try {
            byteArrayDataSource = new ByteArrayDataSource(inputStream, mimeType);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create ByteArrayDataSource from inputStream." + e.getMessage(), e);
        }

        try {
            DataHandler dh = new DataHandler(byteArrayDataSource);
            mimeBodyPart.setDataHandler(dh);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to set data handler on mime body part." + e.getMessage(), e);
        }

        try {
            mimeBodyPart.setHeader("Content-Type", mimeType);
            mimeBodyPart.setHeader("Content-Transfer-Encoding", "binary");   // No content-transfer-encoding needed for http
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to set headers." + e.getMessage(), e);
        }

        return mimeBodyPart;
    }

    /**
     * Calculates sha1 mic based on the MIME body part.
     */
    public static Mic calculateMic(MimeBodyPart bodyPart) {
        String algorithmName = "sha1";
        try {
            MessageDigest md = BCHelper.getMessageDigest(algorithmName);
            bodyPart.writeTo(new DigestOutputStream(ByteStreams.nullOutputStream(), md));
            return new Mic(encoder.encodeToString(md.digest()), algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithmName + " not found", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from digest input. " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to handle mime body part. " + e.getMessage(), e);
        }
    }

    public static String toString(MimeMessage mimeMessage) {
        byte[] bytes = toBytes(mimeMessage);
        return new String(bytes);
    }

    public static byte[] toBytes(MimeMessage mimeMessage) {
        ByteArrayOutputStream evidenceBytes = new ByteArrayOutputStream();
        try {
            mimeMessage.writeTo(evidenceBytes);
        } catch (IOException | MessagingException e) {
            throw new IllegalStateException("Unable to convert MDN mime message into bytes()", e);
        }

        return evidenceBytes.toByteArray();
    }
}
