package eu.peppol.as2.util;

import com.google.common.io.ByteStreams;
import com.sun.mail.util.LineOutputStream;
import eu.peppol.as2.model.As2DispositionNotificationOptions;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * S/MIME helper.
 *
 * @author erlend
 */
public class SMimeReader implements Closeable {

    private MimeMultipart mimeMultipart;

    private byte[] signature;

    private As2DispositionNotificationOptions dispositionNotificationOptions;

    private SMimeDigestMethod sMimeDigestMethod;

    public SMimeReader(MimeMessage mimeMessage) throws MessagingException, IOException {
        this.mimeMultipart = (MimeMultipart) mimeMessage.getContent();

        // Extracting signature
        signature = ByteStreams.toByteArray(((InputStream) mimeMultipart.getBodyPart(1).getContent()));

        // Extracting DNO
        String[] dno = mimeMessage.getHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS);

        if (dno == null)
            throw new IllegalStateException("Unable to extract dno.");

        dispositionNotificationOptions = As2DispositionNotificationOptions.valueOf(dno[0]);
        sMimeDigestMethod = SMimeDigestMethod.findByIdentifier(
                dispositionNotificationOptions.getPreferredSignedReceiptMicAlgorithmName());
    }

    /**
     * Extracts headers of body MIME part. Creates headers as done by Bouncycastle.
     *
     * @return Headers
     */
    public byte[] getBodyHeader() throws MessagingException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LineOutputStream los = new LineOutputStream(outputStream);

        Enumeration hdrLines = ((MimeBodyPart) mimeMultipart.getBodyPart(0)).getNonMatchingHeaderLines(new String[]{});
        while (hdrLines.hasMoreElements())
            los.writeln((String) hdrLines.nextElement());

        // The CRLF separator between header and content
        los.writeln();
        los.close();

        return outputStream.toByteArray();
    }

    /**
     * Extracts content in body MIME part.
     *
     * @return Content
     */
    public InputStream getBodyInputStream() throws MessagingException, IOException {
        return mimeMultipart.getBodyPart(0).getInputStream();
    }

    public SMimeDigestMethod getDigestMethod() {
        return sMimeDigestMethod;
    }

    /**
     * Extracts signature in signature MIME part.
     *
     * @return Signature
     */
    public byte[] getSignature() throws MessagingException, IOException {
        return signature;
    }

    @Override
    public void close() throws IOException {
        mimeMultipart = null;
        signature = null;
    }
}
