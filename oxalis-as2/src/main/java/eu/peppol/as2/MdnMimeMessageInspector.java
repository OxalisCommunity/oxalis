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

import javax.mail.BodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;

/**
 * Inspects the various properties and parts of an MDN wrapped in a S/MIME message.
 *
 * Part 0 : multipart/report; report-type=disposition-notification;
 *      0 : Sub part 0 : text/plain
 *      0 : Sub part 1 : message/disposition-notification
 * Part 1 : application/pkcs7-signature; name=smime.p7s; smime-type=signed-data
 *
 * @author steinar
 * @author thore
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

    public BodyPart getBodyPartAt(int position) {
        try {
            return getMultipartReport().getBodyPart(position);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the body part at position " + position + " : " + e.getMessage(), e);
        }
    }

    public BodyPart getPlainTextBodyPart() {
        return getPartFromMultipartReport("text/plain");
    }

    public BodyPart getMessageDispositionNotificationPart() {
        BodyPart bp = getPartFromMultipartReport("message/disposition-notification");
        if (bp == null) bp = getBodyPartAt(1); // the second part should be machine readable
        return bp;
    }

    private BodyPart getPartFromMultipartReport(String contentType) {
        try {
            MimeMultipart multipartReport = getMultipartReport();
            for (int t = 0; t < multipartReport.getCount(); t++) {
                BodyPart bp = multipartReport.getBodyPart(t);
                //System.out.println("CONTENT TYPE : " + bp.getContentType());
                //System.out.println("CONTENT DATA : " + bp.getContent().toString());
                if (contentType.contains(bp.getContentType())) return bp;
            }
        } catch (Exception e) {
            log.error("Failed to locate part of multipart/report of type " + contentType);
        }
        return null;
    }

    public String getPlainTextPartAsText() {
        try {
            return (String) getPlainTextBodyPart().getContent();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the plain text from the MDN: " + e.getMessage(), e);
        }
    }

    public String getMDNDisposition() {
        try {
            BodyPart bp = getMessageDispositionNotificationPart();
            Object content = bp.getContent();
            if (content instanceof String) return (String) content;
            if (content instanceof InputStream) {
                BufferedReader r = new BufferedReader(new InputStreamReader((InputStream) content));
                while (r.ready()) {
                    String line = r.readLine().trim();
                    if (line.startsWith("Disposition:")) return line;
                }
            }
            throw new Exception("No disposition found, unsupported content type returned @ " + content.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the Disposition from the MDN: " + e.getMessage(), e);
        }
    }

    public boolean isOk() {

        /*
        ------=_Part_172_8810544.1396256987768
        Content-Type: message/disposition-notification
        Content-Transfer-Encoding: 7bit

        Reporting-UA: mendelson opensource AS2
        Original-Recipient: rfc822; APP_1000000002
        Final-Recipient: rfc822; APP_1000000002
        Original-Message-ID: <fd94a0ca-a9bb-4eb2-b3b6-54ff190d0337>
        Disposition: automatic-action/MDN-sent-automatically; processed/error: insufficient-message-security

        ------=_Part_172_8810544.1396256987768--
        */

        /*
        Disposition: "disposition-mode"; processed/Error: decryption-failed
        Disposition: "disposition-mode"; processed/Warning: authentication-failed, processing continued
        Disposition: automatic-action/MDN-sent-automatically; processed
        Disposition: automatic-action/MDN-sent-automatically; processed/error: authentication-failed
        Disposition: automatic-action/MDN-sent-automatically; processed/warning: duplicate-document
        Disposition: automatic-action/MDN-sent-automatically; failed/failure: sender-equals-receiver
        Disposition: automatic-action/MDN-sent-automatically; processed/error: insufficient-message-security
        */

        String disposition = getMDNDisposition().split("Disposition:")[1].trim();
        As2Disposition as2dis = As2Disposition.valueOf(disposition);

        // make sure we are in processed state
        if (!As2Disposition.DispositionType.PROCESSED.equals(as2dis.dispositionType)) {
            log.error("Failed or unknown state : " + disposition);
            return false;
        }

        // make sure we have a "clean processing state"
        As2Disposition.DispositionModifier modifier = as2dis.getDispositionModifier();
        if (modifier == null) return true;

        // allow for partial success (warning)
        if (As2Disposition.DispositionModifier.Prefix.WARNING.equals(modifier.getPrefix())) {
            log.warn("Returns with warning : " + disposition);
            return true;
        }

        log.debug("MDN failed with disposition raw : " + disposition);
        log.debug("MDN failed with as2 disposition : " + as2dis.toString());

        return false;

    }

}
