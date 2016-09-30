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

import eu.peppol.identifier.TransmissionId;

import java.util.Date;

/**
 * Holds an AS2 message which has been received (inbound) over the wire from the PEPPOL network.
 *
 * It can only be created using the As2Message#Builder
 *
 * The builder handles two kinds of input data:
 * <ol>
 *     <li>Textual representation from inbound messages received as S/MIME messages.</li>
 * </ol>
 *
 * @author steinar
 *         Date: 04.10.13
 *         Time: 17:07
 */
public class As2Message {

    // Holds the payload
    private final SignedMimeMessage signedMimeMessage;

    private final String as2Version;
    private final PeppolAs2SystemIdentifier as2From;
    private final PeppolAs2SystemIdentifier as2To;
    private final String subject;
    private final TransmissionId transmissionId;
    private final String date;
    private final As2DispositionNotificationOptions dispositionNotificationOptions;
    private final String receiptDeliveryOption;

    public SignedMimeMessage getSignedMimeMessage() {
        return signedMimeMessage;
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
        sb.append("mimeMessage=").append(signedMimeMessage);
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

        SignedMimeMessage signedMimeMessage;
        private String as2Version = "1.0";
        private PeppolAs2SystemIdentifier as2From;
        private PeppolAs2SystemIdentifier as2To;
        private String subject;
        private String transmissionId;
        private String date;
        private As2DispositionNotificationOptions dispositionNotificationOptions;
        private String receiptDeliveryOption;

        public Builder(SignedMimeMessage signedMimeMessage) {
            this();
            this.signedMimeMessage = signedMimeMessage;
        }

        public Builder() {
            dispositionNotificationOptions = As2DispositionNotificationOptions.valueOf("signed-receipt-protocol=required,pkcs7-signature; signed-receipt-micalg=required,sha1");
        }


        /**
        These are real-world headers from ITSligo which use a commercial AS2 implementation that is Drummond tested
        <pre>
        date: Wed, 02 Apr 2014 08:52:05 GMT
        message-id: <f155f94a-35cd-4047-979a-ce2ee6b89f50@d448d4c2-81c4-46a6-99cb-53cd71feba23>
        mime-version: 1.0
        content-type: multipart/signed; protocol="application/pkcs7-signature"; micalg=sha1; boundary="boundaryrp+YAw=="
        host: ap-test.unit4.com
        x-forwarded-for: 78.19.204.76
        connection: close
        accept-encoding: gzip, deflate
        user-agent: EDI Integrator Component - www.nsoftware.com
        as2-to: APP_1000000006
        as2-from: APP_1000000009
        as2-version: 1.2
        ediint-features: multiple-attachments, AS2-Reliability
        disposition-notification-to: as2@ITSligo.ie
        disposition-notification-options: signed-receipt-protocol=optional, pkcs7-signature; signed-receipt-micalg=optional, sha1
        content-length: 16354
         </pre>
         */
        public As2Message build() {

            required(signedMimeMessage, "mimeMessage");
            required(as2Version, "as2Version");
            required(as2From, "as2From");
            required(as2To, "as2To");
            required(transmissionId, "transmissionId");
            required(date, "date");

            return new As2Message(this);
        }

        public Builder mimeMessage(SignedMimeMessage mimeMessage) {
            this.signedMimeMessage = mimeMessage;
            return this;
        }

        private void required(Object value, String name) {
            if (value == null) {
                throw new IllegalStateException("Must set required header/property '" + name + "'");
            }
        }

        public Builder as2Version(String as2Version) {
            this.as2Version = as2Version;
            return this;
        }

        public Builder as2From(String as2From) throws InvalidAs2HeaderValueException {
            if (as2From == null)
                throw new IllegalArgumentException("as2From requires AS2 identification string");

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
            if (as2To == null) {
                throw new IllegalArgumentException("as2To requires an AS2 identification string");
            }
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
        signedMimeMessage = builder.signedMimeMessage;
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
