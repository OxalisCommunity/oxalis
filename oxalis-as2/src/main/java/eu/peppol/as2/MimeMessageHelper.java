package eu.peppol.as2;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MimeMessageHelper {

    public static MimeMessage createMimeMessage(String msgTxt) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(msgTxt.getBytes());

        return createMimeMessage(byteArrayInputStream);
    }

    public static MimeMessage createMimeMessage(InputStream inputStream) {
        try {
            Properties properties = System.getProperties();
            Session session = Session.getDefaultInstance(properties, null);
            MimeMessage mimeMessage = new MimeMessage(session, inputStream);


            return mimeMessage;
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to create MimeMessage from input stream. " +e.getMessage(),e);
        }
    }

    public static String toString(MimeMessage mimeMessage) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            mimeMessage.writeTo(byteArrayOutputStream);
            return byteArrayOutputStream.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to write Mime message to byte array outbput stream:"+e.getMessage(),e);
        }
    }
}
