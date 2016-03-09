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

import eu.peppol.util.CaseInsensitiveComparator;
import org.apache.commons.codec.binary.Base64InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.BodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Inspects the various properties and parts of an MDN wrapped in a S/MIME message.
 *
 * Part 0 : multipart/report; report-type=disposition-notification;
 *      0 : Sub part 0 : text/plain
 *      0 : Sub part 1 : message/disposition-notification
 *      0 : Sub part x : will not be used by Oxalis
 * Part 1 : application/pkcs7-signature; name=smime.p7s; smime-type=signed-data
 *
 * @author steinar
 * @author thore
 * @author arun
 */
public class MdnMimeMessageInspector {

    public static final Logger log = LoggerFactory.getLogger(MdnMimeMessageInspector.class);

    private final MimeMessage mdnMimeMessage;

    public MdnMimeMessageInspector(MimeMessage mdnMimeMessage) {
        this.mdnMimeMessage = mdnMimeMessage;
    }

    public MimeMultipart getSignedMultiPart() {
        try {
            return (MimeMultipart) mdnMimeMessage.getContent();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to access the contents of the MDN S/MIME message: " + e.getMessage(), e);
        }
    }

    /**
     * The multipart/report should contain both a text/plain part with textual information and
     * a message/disposition-notification part that should be examined for error/failure/warning.
     */
    public MimeMultipart getMultipartReport() {
        try {
            BodyPart bodyPart = getSignedMultiPart().getBodyPart(0);
            Object content = bodyPart.getContent();
            MimeMultipart multipartReport = (MimeMultipart) content;
            if (!multipartReport.getContentType().contains("multipart/report")) {
                throw new IllegalStateException("The first body part of the first part of the signed message is not a multipart/report");
            }
            return multipartReport;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the multipart/report : " + e.getMessage(), e);
        }
    }

    /**
     * We assume that the first text/plain part is the one containing any textual information.
     */
    public BodyPart getPlainTextBodyPart() {
        return getPartFromMultipartReport("text/plain");
    }

    /**
     * We search for the first message/disposition-notification part.
     * If we don't find one of that type we assume that part 2 is the right one.
     */
    public BodyPart getMessageDispositionNotificationPart() {
        BodyPart bp = getPartFromMultipartReport("message/disposition-notification");
        if (bp == null) bp = getBodyPartAt(1); // the second part should be machine readable
        return bp;
    }

    /**
     * Return the fist part which have the given contentType
     * @param contentType the mime type to look for
     */
    private BodyPart getPartFromMultipartReport(String contentType) {
        try {
            MimeMultipart multipartReport = getMultipartReport();
            for (int t = 0; t < multipartReport.getCount(); t++) {
                BodyPart bp = multipartReport.getBodyPart(t);
                if (bp.getContentType().contains(contentType)) return bp;
            }
        } catch (Exception e) {
            log.error("Failed to locate part of multipart/report of type " + contentType);
        }
        return null;
    }

    /**
     * Get a specific part of the multipart/report
     * @param position starts at 0 for the first, 1 for the second, etc
     */
    private BodyPart getBodyPartAt(int position) {
        try {
            return getMultipartReport().getBodyPart(position);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the body part at position " + position + " : " + e.getMessage(), e);
        }
    }

    public String getPlainTextPartAsText() {
        try {
            return (String) getPlainTextBodyPart().getContent();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the plain text from the MDN: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getMdnFields() {

        Map<String, String> ret = new TreeMap<String, String>(new CaseInsensitiveComparator());
        try {

            BodyPart bp = getMessageDispositionNotificationPart();
            boolean contentIsBase64Encoded = false;

            //
            // look for base64 transfer encoded MDN's (when Content-Transfer-Encoding is present)
            //
            // Content-Type: message/disposition-notification
            // Content-Transfer-Encoding: base64
            //
            // "Content-Transfer-Encoding not used in HTTP transport Because HTTP, unlike SMTP,
            // does not have an early history involving 7-bit restriction.
            // There is no need to use the Content Transfer Encodings of MIME."
            //
            String[] contentTransferEncodings = bp.getHeader("Content-Transfer-Encoding");
            if (contentTransferEncodings != null && contentTransferEncodings.length > 0) {
                if (contentTransferEncodings.length > 1) log.warn("MDN has multiple Content-Transfer-Encoding, we only try the first one");
                String encoding = contentTransferEncodings[0];
                if (encoding == null) encoding = "";
                encoding = encoding.trim();
                log.info("MDN specifies Content-Transfer-Encoding : '" + encoding + "'");
                if ("base64".equalsIgnoreCase(encoding)) {
                    contentIsBase64Encoded = true;
                }
            }

            Object content = bp.getContent();
            if (content instanceof InputStream) {
                InputStream contentInputStream = (InputStream) content;
                if (contentIsBase64Encoded) {
                    log.debug("MDN seems to be base64 encoded, wrapping content stream in Base64 decoding stream");
                    contentInputStream = new Base64InputStream(contentInputStream); // wrap in base64 decoding stream
                }
                BufferedReader r = new BufferedReader(new InputStreamReader(contentInputStream));
                while (r.ready()) {
                    String line = r.readLine();
                    int firstColon = line.indexOf(":"); // "Disposition: ......"
                    if (firstColon > 0) {
                        String key = line.substring(0, firstColon).trim(); // up to :
                        String value = line.substring(firstColon + 1).trim(); // skip :
                        ret.put(key, value);
                    }
                }
            } else {
                throw new Exception("Unsupported MDN content, expected InputStream found @ " + content.toString());
            }


        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the values from the MDN : " + e.getMessage(), e);
        }
        return ret;
    }

    /**
     * Decode MDN and make sure the message was processed (allow for warnings)
     * @param outboundMic the outbound mic to verify against
     * @return
     */
    public boolean isOkOrWarning(Mic outboundMic) {

        Map<String, String> ret = getMdnFields();

        /*
        --------_=_NextPart_001_B096DD27.9007A6CE
        Content-Type: message/disposition-notification

        Reporting-UA: AS2 eefacta Server (unimaze.com)
        Original-Recipient: rfc822; SMP_2000000005
        Final-Recipient: rfc822; SMP_2000000005
        Original-Message-ID: a60d9982-680c-4f01-9ab4-9b5d5fb05f37
        Received-Content-MIC: ZMY/AoJb2JQS557MOATtc0EZdZQ=, sha1
        Disposition: automatic-action/MDN-sent-automatically; processed


        --------_=_NextPart_001_B096DD27.9007A6CE--
        */

        // make sure we have a valid disposition
        String disposition = ret.get("Disposition");
        if (disposition == null) {
            log.error("Unable to retreieve 'Disposition' from MDN");
            return false;
        }

        log.info("Decoding received disposition ({})", disposition);
        As2Disposition as2dis = As2Disposition.valueOf(disposition);

        // make sure we are in processed state
        if (!As2Disposition.DispositionType.PROCESSED.equals(as2dis.dispositionType)) {
            // Disposition: automatic-action/MDN-sent-automatically; failed/failure: sender-equals-receiver
            log.error("Failed or unknown state : " + disposition);
            return false;
        }

        // check if the returned MIC matches our outgoing MIC (sha1 of payload), warn about mic mismatch
        String receivedMic = ret.get("Received-Content-MIC");
        if (receivedMic != null) {
            if (!outboundMic.toString().equalsIgnoreCase(Mic.valueOf(receivedMic).toString())) {
                log.warn("MIC mismatch, Received-Content-MIC was : " + receivedMic + " while Outgoing-MIC was : " + outboundMic.toString());
            }
        } else {
            log.error("MIC error, no Received-Content-MIC returned in MDN");
        }

        // return when "clean processing state" : Disposition: automatic-action/MDN-sent-automatically; processed
        As2Disposition.DispositionModifier modifier = as2dis.getDispositionModifier();
        if (modifier == null) return true;

        // allow partial success (warning)
        if (As2Disposition.DispositionModifier.Prefix.WARNING.equals(modifier.getPrefix())) {
            // Disposition: automatic-action/MDN-sent-automatically; processed/warning: duplicate-document
            log.warn("Returns with warning : " + disposition);
            return true;
        }

        // Disposition: automatic-action/MDN-sent-automatically; processed/error: insufficient-message-security
        log.debug("MDN failed with disposition raw : " + disposition);
        log.debug("MDN failed with as2 disposition : " + as2dis.toString());

        return false;

    }

}
