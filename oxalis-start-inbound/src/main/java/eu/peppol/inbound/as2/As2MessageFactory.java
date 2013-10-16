package eu.peppol.inbound.as2;

import eu.peppol.as2.As2Header;
import eu.peppol.as2.As2Message;
import eu.peppol.as2.InvalidAs2HeaderException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Security;
import java.util.Properties;

/**
 * Extracts data from an HttpServletRequest and builds an As2Message.
 *
 * @author steinar
 *         Date: 07.10.13
 *         Time: 21:34
 */
public class As2MessageFactory {

    public static As2Message createAs2MessageFrom(HttpServletRequest servletRequest) throws InvalidAs2HeaderException {
        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        MimeMessage mimeMessage = createMimeMessage(servletRequest);

        As2Message.Builder builder = new As2Message.Builder(mimeMessage);
        builder.as2Version(servletRequest.getHeader(As2Header.AS2_VERSION.getHttpHeaderName()));
        builder.as2From(servletRequest.getHeader(As2Header.AS2_FROM.getHttpHeaderName()));
        builder.as2To(servletRequest.getHeader(As2Header.AS2_TO.getHttpHeaderName()));
        builder.date(servletRequest.getHeader(As2Header.DATE.getHttpHeaderName()));
        builder.subject(servletRequest.getHeader(As2Header.SUBJECT.getHttpHeaderName()));
        builder.messageId(servletRequest.getHeader(As2Header.MESSAGE_ID.getHttpHeaderName()));
        builder.dispositionNotificationOptions(servletRequest.getHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName()));
        builder.receiptDeliveryOption(servletRequest.getHeader(As2Header.RECEIPT_DELIVERY_OPTION.getHttpHeaderName()));
        return builder.build();
    }


    private static MimeMessage createMimeMessage(HttpServletRequest servletRequest) {
        try {
            Properties properties = System.getProperties();
            Session session = Session.getDefaultInstance(properties, null);
            MimeMessage mimeMessage = new MimeMessage(session, servletRequest.getInputStream());
            return mimeMessage;
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create MimeMessage from input stream. " +e.getMessage(),e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data into MimeMessage " + e.getMessage(), e);
        }
    }
}
