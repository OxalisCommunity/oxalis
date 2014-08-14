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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import javax.activation.DataHandler;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
    private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;

    /**
     * Creates a simple MimeMessage with a Mime type of text/plain with a single MimeBodyPart
     */
    public static MimeMessage createSimpleMimeMessage(String msgTxt) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(msgTxt.getBytes());
        try {
            MimeType mimeType = new MimeType("text", "plain");
            MimeBodyPart mimeBodyPart = createMimeBodyPart(byteArrayInputStream, mimeType);
            MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
            mimeMessage.setContent(mimeMessage, mimeType.toString());
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
            ByteArrayDataSource dataSource = new ByteArrayDataSource(inputStream, mimeType.getBaseType());
            return multipartMimeMessage(dataSource);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static MimeMessage parseMultipart(String contents, MimeType mimeType) {
        try {
            ByteArrayDataSource dataSource = new ByteArrayDataSource(contents, mimeType.getBaseType());
            return multipartMimeMessage(dataSource);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create ByteArrayDataSource; " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create Multipart mime message; " + e.getMessage(), e);
        }
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
            MessageDigest md = MessageDigest.getInstance(algorithmName, PROVIDER_NAME);
            md.update(content);
            byte[] digest = md.digest();
            String digestAsString = new String(Base64.encode(digest));
            return new Mic(digestAsString, algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithmName + " not found", e);
        } catch (NoSuchProviderException e) {
            throw new IllegalStateException("Security provider " + PROVIDER_NAME + " not found. Do you have BouncyCastle on your path?");
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
