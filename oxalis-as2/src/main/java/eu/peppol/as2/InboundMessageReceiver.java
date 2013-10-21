package eu.peppol.as2;

import eu.peppol.as2.As2DispositionNotificationOptions;
import eu.peppol.as2.As2Header;
import eu.peppol.as2.MdnData;
import eu.peppol.as2.MdnRequestException;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.mail.smime.SMIMESignedParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * @author steinar
 *         Date: 20.10.13
 *         Time: 10:45
 */
public class InboundMessageReceiver {

    public static final Logger log = LoggerFactory.getLogger(InboundMessageReceiver.class);

    public InboundMessageReceiver() {


    }

    public MdnData receive(Map<String, String> mapOfHeaders, InputStream inputStream) throws ErrorWithMdnException {

        try {
            // Inspects the eu.peppol.as2.As2Header.DISPOSITION_NOTIFICATION_OPTIONS
            inspectDispositionNotificationOptions(mapOfHeaders);
            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(mapOfHeaders, inputStream);
            // Validates the message headers according to the PEPPOL rules
            // Performs semantic validation
            MimeMessageInspector mimeMessageInspector = As2MessageInspector.validate(as2Message);

            // Persists the payload

            // Calculates the MIC for the payload

            // Creates the MDN to be returned
        } catch (InvalidAs2MessageException e) {
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(mapOfHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);
        } catch (MdnRequestException e) {
            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(mapOfHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);
        }

        MdnData mdnData = MdnData.Builder.buildProcessedOK(mapOfHeaders);

        return mdnData;
    }

    private As2DispositionNotificationOptions inspectDispositionNotificationOptions(Map<String, String> map) throws MdnRequestException {

        String headerValue = map.get(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName());
        if (headerValue == null) {
            throw new MdnRequestException("AS2 header " + As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName() + " not found in request");
        }

        // Attempts to parse the Disposition Notification Options
        return As2DispositionNotificationOptions.valueOf(headerValue);
    }
}