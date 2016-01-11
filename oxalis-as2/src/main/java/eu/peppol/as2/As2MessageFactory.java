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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Security;

import static eu.peppol.as2.HeaderUtil.getFirstValue;

/**
 * Extracts data from a Map of headers and an InputStream and builds an As2Message, which contains the payload
 * inside a signed MIME message.
 *
 * @author steinar
 *         Date: 07.10.13
 *         Time: 21:34
 */
public class As2MessageFactory {

    public static final Logger log = LoggerFactory.getLogger(As2MessageFactory.class);

    /**
     * Creates a MIME message, an As2Message and adds the MIME message into it.
     *
     * @param internetHeaders
     * @param signedMimeMessage
     * @return
     * @throws InvalidAs2MessageException
     * @throws MdnRequestException
     */
    public static As2Message createAs2MessageFrom(InternetHeaders internetHeaders, SignedMimeMessage signedMimeMessage) throws InvalidAs2MessageException, MdnRequestException {

        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        // Creates the As2Message builder, into which the headers are added
        As2Message.Builder builder = createAs2MessageBuilder(internetHeaders);

        // Adds the MIME message to the As2Message structure
        builder.mimeMessage(signedMimeMessage);

        return builder.build();

    }

    static As2Message.Builder createAs2MessageBuilder(InternetHeaders internetHeaders) throws InvalidAs2HeaderValueException, MdnRequestException {
        As2Message.Builder builder = new As2Message.Builder();

        builder.as2Version(getFirstValue(internetHeaders, As2Header.AS2_VERSION.getHttpHeaderName()));
        builder.as2From(getFirstValue(internetHeaders, As2Header.AS2_FROM.getHttpHeaderName()));
        builder.as2To(getFirstValue(internetHeaders, As2Header.AS2_TO.getHttpHeaderName()));
        builder.date(getFirstValue(internetHeaders, As2Header.DATE.getHttpHeaderName()));
        builder.subject(getFirstValue(internetHeaders, As2Header.SUBJECT.getHttpHeaderName()));
        builder.transmissionId(getFirstValue(internetHeaders, As2Header.MESSAGE_ID.getHttpHeaderName()));

        // Any errors during parsing of  disposition-notification-options header, needs special treatment as
        // this is the special case which mandates the use of "failed" rather than "processed" in the
        // the "disposition" header of the MDN returned to the sender.
        // See section 7.5.3 of RFC4130
        try {
            String dispositionNotificationOptions = getFirstValue(internetHeaders, As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName());
            builder.dispositionNotificationOptions(dispositionNotificationOptions);
        } catch (Exception e) {
            throw new MdnRequestException(e.getMessage());
        }
        builder.receiptDeliveryOption(getFirstValue(internetHeaders, As2Header.RECEIPT_DELIVERY_OPTION.getHttpHeaderName()));

        return builder;
    }

    /**
     * Dumps the supplied mime message to the logger.
     *
     * @param mimeMessage contains the mime message to be printed in plain
     */
    private static void dump(MimeMessage mimeMessage) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            mimeMessage.writeTo(baos);
            log.debug(baos.toString());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MessagingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
