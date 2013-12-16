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

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;

/**
 * Inspects the various properties and parts of an MDN wrapped in a S/MIME message
 *
 * @author steinar
 *         Date: 28.10.13
 *         Time: 13:43
 */
public class MdnMimeMessageInspector {
    private final MimeMessage mdnMimeMessage;

    private BodyPart plainTextBodyPart;

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

    public BodyPart getPlainTextBodyPart() {
        try {
            BodyPart bodyPart = getSignedMultiPart().getBodyPart(0);
            Object content = bodyPart.getContent();
            MimeMultipart multipartReport = (MimeMultipart) content;
            if (!multipartReport.getContentType().contains("multipart/report")) {
                throw new IllegalStateException("The first body part of the first part of the signed message is not a multipart/report");
            }

            return multipartReport.getBodyPart(0);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the plain text body part: " + e.getMessage(), e);
        }
    }

    public String getPlainText() {
        try {
            return (String) getPlainTextBodyPart().getContent();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to retrieve the plain text from the MDN: " + e.getMessage(), e);
        }
    }
}
