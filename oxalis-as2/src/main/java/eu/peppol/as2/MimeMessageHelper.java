/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.as2;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Collection of useful methods for manipulating MIME messages.
 *
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 * @author Arun Kumar
 */
public class MimeMessageHelper {

	public static final Logger log = LoggerFactory.getLogger(MimeMessageHelper.class);

    /**
     * Creates a simple MimeMessage with a Mime type of text/plain with a single MimeBodyPart
     */
    public static MimeMessage createSimpleMimeMessage(String msgTxt) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(msgTxt.getBytes());
        try {
            MimeType mimeType = new MimeType("text", "plain");
            MimeBodyPart mimeBodyPart = createMimeBodyPart(byteArrayInputStream, mimeType);
            MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
            mimeMessage.setContent(mimeBodyPart, mimeType.toString());
            return mimeMessage;
        } catch (MimeTypeParseException e) {
            throw new IllegalArgumentException("Unable to create MimeType" + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to set content of mime message " + e.getMessage(), e);
        }
    }

    /**
     * Creates a MIME message from the supplied stream, which <em>must</em> contain headers,
     * especially the header "Content-Type:"
     */
    public static MimeMessage createMimeMessage(InputStream inputStream) {
        try {
            Properties properties = System.getProperties();
            Session session = Session.getDefaultInstance(properties, null);
            MimeMessage mimeMessage = new MimeMessage(session, inputStream);
            return mimeMessage;
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create MimeMessage from input stream. " + e.getMessage(), e);
        }
    }

    /**
     * Creates a MimeMultipart MIME message from an input stream, which does not contain the header "Content-Type:".
     * Thus the mime type must be supplied as an argument.
     */
    public static MimeMessage parseMultipart(InputStream inputStream, MimeType mimeType) {
        try {
            ByteArrayDataSource dataSource = new ByteArrayDataSource(inputStream, mimeType.toString());
            return multipartMimeMessage(dataSource);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static MimeMessage parseMultipart(String contents, MimeType mimeType) {
        try {
            ByteArrayDataSource dataSource = new ByteArrayDataSource(contents, mimeType.toString());
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
     *
     * @param inputStream
     * @param headers
     * @return
     */
    public static MimeMessage createMimeMessageAssistedByHeaders(InputStream inputStream, InternetHeaders headers) throws InvalidAs2MessageException {
        MimeType mimeType = null;
        String contentType = headers.getHeader("Content-Type", ",");
        if (contentType != null) {
            try {
                // From rfc2616 :
                // Multiple message-header fields with the same field-name MAY be present in a message if and only
                // if the entire field-value for that header field is defined as a comma-separated list.
                // It MUST be possible to combine the multiple header fields into one "field-name: field-value" pair,
                // without changing the semantics of the message, by appending each subsequent field-value to the first,
                // each separated by a comma.
                mimeType = new MimeType(contentType);
            } catch (MimeTypeParseException e) {
                log.warn("Unable to MimeType from content type '" + contentType + "', defaulting to createMimeMessage() from body : " + e.getMessage());
            }
        }
        if (mimeType == null) {
            log.warn("Headers did not contain MIME content type, trying to decode content type from body.");
            return MimeMessageHelper.parseMultipart(inputStream);
        }
        return MimeMessageHelper.parseMultipart(inputStream, mimeType);
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

    public static MimeBodyPart createMimeBodyPart(InputStream inputStream, MimeType mimeType) {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        ByteArrayDataSource byteArrayDataSource = null;

        try {
            byteArrayDataSource = new ByteArrayDataSource(inputStream, mimeType.toString());
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
            mimeBodyPart.setHeader("Content-Type", mimeType.toString());
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bodyPart.writeTo(baos);
            byte[] content = baos.toByteArray();
            MessageDigest md = MessageDigest.getInstance(algorithmName, new BouncyCastleProvider());
            md.update(content);
            byte[] digest = md.digest();
            String digestAsString = new String(Base64.encode(digest));
            return new Mic(digestAsString, algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithmName + " not found", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from digest input. " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to handle mime body part. " + e.getMessage(), e);
        }
    }

    public static String toString(MimeMessage mimeMessage) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            mimeMessage.writeTo(byteArrayOutputStream);
            return byteArrayOutputStream.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to write Mime message to byte array outbput stream:" + e.getMessage(), e);
        }
    }

    public static void dumpMimePartToFile(String filename, MimePart mimePart) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mimePart.writeTo(bos);
            FileOutputStream fos = new FileOutputStream(filename);
            bos.writeTo(fos);
            fos.close();
        } catch (Exception ex) {
            log.error("Unable to dumpMimePartToFile(" + filename + ") : " + ex.getMessage());
        }
    }

}
