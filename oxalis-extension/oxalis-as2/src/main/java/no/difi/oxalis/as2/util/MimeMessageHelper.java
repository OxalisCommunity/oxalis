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
import no.difi.oxalis.commons.bouncycastle.BCHelper;
import no.difi.vefa.peppol.common.model.Digest;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collection of useful methods for manipulating MIME messages.
 *
 * @author Steinar Overbeck Cook
 * @author Thore Johnsen
 * @author Arun Kumar
 */
public class MimeMessageHelper {

    private static final Session SESSION = Session.getDefaultInstance(System.getProperties(), null);

    /**
     * Creates a MIME message from the supplied stream, which <em>must</em> contain headers,
     * especially the header "Content-Type:"
     */
    public static MimeMessage parse(InputStream inputStream) throws MessagingException {
        return new MimeMessage(SESSION, inputStream);
    }

    /**
     * Creates a MIME message from the supplied InputStream, using values from the HTTP headers to
     * do a successful MIME decoding.
     */
    public static MimeMessage parse(InputStream inputStream, InternetHeaders headers)
            throws MessagingException {

        return parse(inputStream,
                Collections.list((Enumeration<? extends Object>) headers.getAllHeaderLines())
                        .stream()
                        .map(String.class::cast));
    }

    /**
     * Parses a complete MIME message with provided headers.
     *
     * @param inputStream Content part of MIME message.
     * @param headers     Headers provided as a stream of Strings.
     * @return Parsed MIME message.
     * @throws MessagingException Thrown when content is successfully parsed.
     * @since 4.0.2
     */
    public static MimeMessage parse(InputStream inputStream, Stream<String> headers)
            throws MessagingException {

        // Read headers to a string
        String headerString = headers.collect(Collectors.joining("\r\n")) + "\r\n\r\n";

        // Parse content
        return parse(new SequenceInputStream(
                new ByteArrayInputStream(headerString.getBytes()),
                inputStream
        ));
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
    public static Digest calculateMic(MimeBodyPart bodyPart, SMimeDigestMethod digestMethod) {
        try {
            MessageDigest md = BCHelper.getMessageDigest(digestMethod.getIdentifier());
            bodyPart.writeTo(new DigestOutputStream(ByteStreams.nullOutputStream(), md));
            return Digest.of(digestMethod.getDigestMethod(), md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(digestMethod.getIdentifier() + " not found", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from digest input. " + e.getMessage(), e);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to handle mime body part. " + e.getMessage(), e);
        }
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
