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

import eu.peppol.identifier.TransmissionId;

import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * Holds an AS2 message which has either been received (inbound) over the wire from the PEPPOL network.
 *
 * It can only be created using the As2Message#Builder
 *
 * The builder handles two kinds of input data:
 * <ol>
 *     <li>Structured, strongly typed for outbound message originating from our own code.</li>
 *     <li>Textual representation from inbound messages received as S/MIME messages.</li>
 * </ol>
 *
 * @author steinar
 *         Date: 04.10.13
 *         Time: 17:07
 */
public class As2Message {

    // Holds the payload
    private final MimeMessage mimeMessage;

    private final String as2Version;
    private final PeppolAs2SystemIdentifier as2From;
    private final PeppolAs2SystemIdentifier as2To;
    private final String subject;
    private final TransmissionId transmissionId;
    private final String date;
    private final As2DispositionNotificationOptions dispositionNotificationOptions;
    private final String receiptDeliveryOption;

    public MimeMessage getMimeMessage() {
        return mimeMessage;
    }

    public String getAs2Version() {
        return as2Version;
    }

    public PeppolAs2SystemIdentifier getAs2From() {
        return as2From;
    }

    public As2SystemIdentifier getAs2To() {
        return as2To;
    }

    public String getSubject() {
        return subject;
    }

    public TransmissionId getTransmissionId() {
        return transmissionId;
    }

    public String getDate() {
        return date;
    }

    public As2DispositionNotificationOptions getDispositionNotificationOptions() {
        return dispositionNotificationOptions;
    }

    public String getReceiptDeliveryOption() {
        return receiptDeliveryOption;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("As2Message{");
        sb.append("mimeMessage=").append(mimeMessage);
        sb.append(", as2Version='").append(as2Version).append('\'');
        sb.append(", as2From=").append(as2From);
        sb.append(", as2To=").append(as2To);
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", transmissionId='").append(transmissionId).append('\'');
        sb.append(", date='").append(date).append('\'');
        sb.append(", dispositionNotificationOptions=").append(dispositionNotificationOptions);
        sb.append(", receiptDeliveryOption='").append(receiptDeliveryOption).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {

        MimeMessage mimeMessage;
        private String as2Version = "1.0";
        private PeppolAs2SystemIdentifier as2From;
        private PeppolAs2SystemIdentifier as2To;
        private String subject;
        private String transmissionId;
        private String date;
        private As2DispositionNotificationOptions dispositionNotificationOptions;
        private String receiptDeliveryOption;

        public Builder(MimeMessage mimeMessage) {
            this();
            this.mimeMessage = mimeMessage;
        }

        public Builder() {
            dispositionNotificationOptions = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=required, pkcs7-signature; signed-receipt-micalg=required,sha1");
        }


        public As2Message build() {

            required(mimeMessage, "mimeMessage");
            required(as2Version, "as2Version");
            required(as2From, "as2From");
            required(as2To, "as2To");
            required(subject, "subject");
            required(transmissionId, "transmissionId");
            required(date, "date");

            return new As2Message(this);
        }

        public Builder mimeMessage(MimeMessage mimeMessage) {
            this.mimeMessage = mimeMessage;
            return this;
        }

        private void required(Object value, String name) {
            if (value == null) {
                throw new IllegalStateException("Must set required header/proeprty '" + name + "'");
            }
        }

        public Builder as2Version(String as2Version) {
            this.as2Version = as2Version;
            return this;
        }

        public Builder as2From(String as2From) throws InvalidAs2HeaderValueException {
            try {
                this.as2From = new PeppolAs2SystemIdentifier(as2From);
            } catch (InvalidAs2SystemIdentifierException invalidAs2SystemIdentifierException) {
                throw new InvalidAs2HeaderValueException(As2Header.AS2_FROM, as2From);
            }
            return this;
        }

        public Builder as2From(PeppolAs2SystemIdentifier as2SystemIdentifier) {
            this.as2From = as2SystemIdentifier;
            return this;
        }

        public Builder as2To(String as2To) {
            try {
                this.as2To = new PeppolAs2SystemIdentifier(as2To);
            } catch (InvalidAs2SystemIdentifierException e) {
                throw new IllegalArgumentException(as2To + " is an invalid PEPPOL AS2 System identifier " + e, e);
            }
            return this;
        }

        public Builder as2To(PeppolAs2SystemIdentifier as2To) throws InvalidAs2HeaderValueException {
                this.as2To = as2To;
            return this;
        }

        public Builder subject(String subject) {

            this.subject = subject;
            return this;
        }

        /**
         * The unique identification of a transmission, held in the "Message-ID" header of the
         * AS2 protocol.
         *
         * @param messageId the value of the AS2 Header field "Message-ID"
         * @return this
         */
        public Builder transmissionId(String messageId) {

            this.transmissionId = messageId;
            return this;
        }

        public Builder date(Date date) {
            this.date = As2DateUtil.format(date);
            return this;
        }

        public Builder date(String date) {
            this.date = date;
            return this;
        }

        public Builder dispositionNotificationOptions(String dispositionNotificationOptions)  {
            this.dispositionNotificationOptions = As2DispositionNotificationOptions.valueOf(dispositionNotificationOptions);
            return this;
        }

        public Builder receiptDeliveryOption(String receiptDeliveryOption) {
            this.receiptDeliveryOption = receiptDeliveryOption;
            return this;
        }
    }

    private As2Message(Builder builder) {
        mimeMessage = builder.mimeMessage;
        as2Version = builder.as2Version;
        as2From = builder.as2From;
        as2To = builder.as2To;
        transmissionId = new TransmissionId(builder.transmissionId);
        subject = builder.subject;
        date = builder.date;
        dispositionNotificationOptions = builder.dispositionNotificationOptions;
        receiptDeliveryOption = builder.receiptDeliveryOption;
    }
}
