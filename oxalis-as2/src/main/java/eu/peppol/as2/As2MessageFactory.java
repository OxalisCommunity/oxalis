package eu.peppol.as2;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Map;

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
     * @param headerMap
     * @param inputStream
     * @return
     * @throws InvalidAs2MessageException
     * @throws MdnRequestException
     */
    public static As2Message createAs2MessageFrom(Map<String, String> headerMap, InputStream inputStream) throws InvalidAs2MessageException, MdnRequestException {
        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        MimeMessage mimeMessage = createMimeMessage(inputStream);

        // dump(mimeMessage);

        // Creates the As2Message builder, into which the headers are added
        As2Message.Builder builder = createAs2MessageBuilder(headerMap);

        // Adds the MIME message to the As2Message structure
        builder.mimeMessage(mimeMessage);

        return builder.build();

    }


    static As2Message.Builder createAs2MessageBuilder(Map<String, String> map) throws InvalidAs2HeaderValueException, MdnRequestException {
        As2Message.Builder builder = new As2Message.Builder();

        builder.as2Version(map.get(As2Header.AS2_VERSION.getHttpHeaderName()));
        builder.as2From(map.get(As2Header.AS2_FROM.getHttpHeaderName()));
        builder.as2To(map.get(As2Header.AS2_TO.getHttpHeaderName()));
        builder.date(map.get(As2Header.DATE.getHttpHeaderName()));
        builder.subject(map.get(As2Header.SUBJECT.getHttpHeaderName()));
        builder.messageId(map.get(As2Header.MESSAGE_ID.getHttpHeaderName()));

        // Any errors during parsing of  disposition-notification-options header, needs special treatment as
        // this is the special case which mandates the use of "failed" rather than "processed" in the
        // the "disposition" header of the MDN returned to the sender.
        // See section 7.5.3 of RFC4130
        try {
            String dispositionNotificationOptions = map.get(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName());
            builder.dispositionNotificationOptions(dispositionNotificationOptions);
        } catch (Exception e) {
            throw new MdnRequestException(e.getMessage());
        }
        builder.receiptDeliveryOption(map.get(As2Header.RECEIPT_DELIVERY_OPTION.getHttpHeaderName()));

        return builder;
    }


    /**
     * Creates a MIME message from the supplied InputStream, which should start providing data from the first  header
     * of the message. I.e. typically after the first "blank line" in a HTTP POST'ing.
     *
     *
     * @param inputStream
     * @return
     * @throws InvalidAs2MessageException
     */
    public static MimeMessage createMimeMessage(InputStream inputStream) throws InvalidAs2MessageException {
        return MimeMessageHelper.parseMultipart(inputStream);
    }

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
