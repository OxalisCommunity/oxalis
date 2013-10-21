package eu.peppol.as2;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Map;
import java.util.Properties;

/**
 * Extracts data from a Map of headers and an InputStream and builds an As2Message.
 *
 * @author steinar
 *         Date: 07.10.13
 *         Time: 21:34
 */
public class As2MessageFactory {

    public static final Logger log = LoggerFactory.getLogger(As2MessageFactory.class);

    public static As2Message createAs2MessageFrom(Map<String, String> map, InputStream inputStream) throws InvalidAs2MessageException, MdnRequestException {
        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        // Adds the Mime message last
        MimeMessage mimeMessage = createMimeMessage(inputStream);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            mimeMessage.writeTo(baos);
            log.debug(baos.toString());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MessagingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        As2Message.Builder builder = new As2Message.Builder();
        builder.mimeMessage(mimeMessage);

        builder.as2Version(map.get(As2Header.AS2_VERSION.getHttpHeaderName()));
        builder.as2From(map.get(As2Header.AS2_FROM.getHttpHeaderName()));
        builder.as2To(map.get(As2Header.AS2_TO.getHttpHeaderName()));
        builder.date(map.get(As2Header.DATE.getHttpHeaderName()));
        builder.subject(map.get(As2Header.SUBJECT.getHttpHeaderName()));
        builder.messageId(map.get(As2Header.MESSAGE_ID.getHttpHeaderName()));
        try {
            String dispositionNotificationOptions = map.get(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName());
            builder.dispositionNotificationOptions(dispositionNotificationOptions);
        } catch (Exception e) {
            throw new MdnRequestException(e.getMessage());
        }
        builder.receiptDeliveryOption(map.get(As2Header.RECEIPT_DELIVERY_OPTION.getHttpHeaderName()));

        return builder.build();

    }

    public static MimeMessage createMimeMessage(InputStream inputStream) throws InvalidAs2MessageException {
        try {
            Properties properties = System.getProperties();
            Session session = Session.getDefaultInstance(properties, null);
            MimeMessage mimeMessage = new MimeMessage(session, inputStream);


            return mimeMessage;
        } catch (MessagingException e) {
            throw new InvalidAs2MessageException("Unable to create MimeMessage from input stream. " +e.getMessage(),e);
        }
    }
}
