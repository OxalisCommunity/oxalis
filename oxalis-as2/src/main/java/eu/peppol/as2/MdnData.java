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

import javax.mail.internet.InternetHeaders;
import java.util.Date;

import static eu.peppol.as2.HeaderUtil.getFirstValue;

/**
 * Holds the data in a Message Disposition Notification (MDN).
 * Instances of this class must be transformed into a MIME message for transmission.
 *
 * @see MdnMimeMessageFactory
 * @author steinar
 *         Date: 09.10.13
 *         Time: 21:01
 */
public class MdnData {

    public static final String SUBJECT = "AS2 MDN as you requested";
    private final String subject;
    private final String as2From;
    private final String as2To;
    private final As2Disposition as2Disposition;
    private final Mic mic;
    private Date date;
    private String messageId;

    private MdnData(Builder builder) {
        this.subject = builder.subject;
        this.as2From = builder.as2From;
        this.as2To = builder.as2To;
        this.as2Disposition = builder.disposition;
        this.mic = builder.mic;
        this.date = builder.date;
        this.messageId = builder.messageId;
    }

    public String getSubject() {
        return subject;
    }

    public String getAs2From() {
        return as2From;
    }

    public String getAs2To() {
        return as2To;
    }

    public As2Disposition getAs2Disposition() {
        return as2Disposition;
    }

    public Mic getMic() {
        return mic;
    }

    public Date getDate() {
        return date;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MdnData {");
        sb.append("subject='").append(subject).append('\'');
        sb.append(", as2From='").append(as2From).append('\'');
        sb.append(", as2To='").append(as2To).append('\'');
        sb.append(", as2Disposition=").append(as2Disposition);
        sb.append(", mic='").append(mic).append('\'');
        sb.append(", date=").append(As2DateUtil.format(date));
        sb.append(", messageId='").append(messageId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {

        String subject = "No subject";
        String as2From = "No_AS2From";
        String as2To = "No_AS2To";

        As2Disposition disposition;

        Mic mic = new Mic("","");
        Date date = new Date();
        String messageId = "";

        public Builder date(Date date){
            this.date = date;
            return this;
        }

        Builder subject(String subject) {
            if (subject != null) {
                this.subject = subject;
            }
            return this;
        }

        Builder as2From(String as2From) {
            this.as2From = as2From;
            return this;
        }

        Builder as2To(String as2To) {
            this.as2To = as2To;
            return this;
        }

        Builder disposition(As2Disposition disposition) {
            this.disposition = disposition;
            return this;
        }

        Builder mic(Mic mic) {
            this.mic = mic;
            return this;
        }

        Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        MdnData build() {
            required(as2From, "as2From");
            required(as2To, "as2To");
            required(disposition, "disposition");
            return new MdnData(this);
        }

        private void required(Object object, String name) {
            if (object == null) {
                throw new IllegalStateException("Required property '" + name + "' not set.");
            }
        }

        public static MdnData buildProcessedOK(InternetHeaders headers, Mic mic) {
            Builder builder = new Builder();
            builder.disposition(As2Disposition.processed()).mic(mic);
            addStandardHeaders(headers, builder);
            return new MdnData(builder);
        }

        public static MdnData buildFailureFromHeaders(InternetHeaders map, Mic mic, String msg) {
            Builder builder = new Builder();
            builder.disposition(As2Disposition.failed(msg)).mic(mic);
            addStandardHeaders(map, builder);
            return new MdnData(builder);
        }

        public static MdnData buildProcessingErrorFromHeaders(InternetHeaders headers, Mic mic, String msg) {
            Builder builder = new Builder();
            builder.disposition(As2Disposition.processedWithError(msg)).mic(mic);
            addStandardHeaders(headers, builder);
            return new MdnData(builder);
        }

        private static void addStandardHeaders(InternetHeaders headers, Builder builder) {
            builder.as2From(getFirstValue(headers, As2Header.AS2_TO.getHttpHeaderName()))
                    .as2To(getFirstValue(headers, As2Header.AS2_FROM.getHttpHeaderName()))
                    .date(new Date())
                    .subject(SUBJECT)
                    .messageId(getFirstValue(headers, As2Header.MESSAGE_ID.getHttpHeaderName()));
        }

    }

}
