package eu.peppol.as2;

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.document.DocumentSniffer;
import eu.peppol.document.SbdhParser;
import eu.peppol.identifier.TransmissionId;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.security.CommonName;
import eu.peppol.identifier.AccessPointIdentifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.InternetHeaders;
import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.security.Security;

/**
 * Main entry point for receiving AS2 messages.
 *
 * @author steinar
 *         Date: 20.10.13
 *         Time: 10:45
 */
public class InboundMessageReceiver {

    public static final Logger log = LoggerFactory.getLogger(InboundMessageReceiver.class);
    private final SbdhParser sbdhParser;

    public InboundMessageReceiver() {
        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());
        sbdhParser = new SbdhParser();
    }

    /**
     * Receives an AS2 Message in the form of a map of headers together with the payload, which is made available
     * in an input stream
     *
     *
     *
     *
     *
     * @param internetHeaders
     * @param inputStream  supplies the actual data
     * @param messageRepository
     * @return MDN object if everything is ok.
     * @throws ErrorWithMdnException if validation fails due to syntactic, semantic or other reasons.
     */
    public MdnData receive(InternetHeaders internetHeaders, InputStream inputStream, MessageRepository messageRepository) throws ErrorWithMdnException {

        if (messageRepository == null) {
            throw new IllegalArgumentException("messageRepository is a required argument in constructor");
        }
        if (internetHeaders == null) {
            throw new IllegalArgumentException("internetHeaders required constructor argument");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream required constructor argument");
        }
        try {
            log.info("Receiving message ..");
            // Inspects the eu.peppol.as2.As2Header.DISPOSITION_NOTIFICATION_OPTIONS
            inspectDispositionNotificationOptions(internetHeaders);

            log.info("Message contains valid Disposition-notification-options, now creating internal AS2 message...");
            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(internetHeaders, inputStream);

            log.info("Validating AS2 Message: " + as2Message);

            // Validates the message headers according to the PEPPOL rules
            // Performs semantic validation
            SignedMimeMessageInspector SignedMimeMessageInspector = As2MessageInspector.validate(as2Message);

            // Persists the payload
            InputStream payloadInputStream = SignedMimeMessageInspector.getPayload();

            PeppolMessageMetaData peppolMessageMetaData = collectTransmissionData(as2Message, SignedMimeMessageInspector);

            log.info("Persisting AS2 Message ....");
            messageRepository.saveInboundMessage(peppolMessageMetaData, payloadInputStream);

//                smimeMessageInspector.getMimeMessage().writeTo(System.out);


            log.info("Persisting statistics");

            // Calculates the MIC for the payload
            As2DispositionNotificationOptions.Parameter signedReceiptMicalg = as2Message.getDispositionNotificationOptions().getSignedReceiptMicalg();
            String micAlgorithmName = signedReceiptMicalg.getTextValue();
            Mic mic = SignedMimeMessageInspector.calculateMic(micAlgorithmName);

            // Creates the MDN to be returned
            MdnData mdnData = MdnData.Builder.buildProcessedOK(internetHeaders, mic);
            log.info("Message received OK, MDN returned: " + mdnData);
            return mdnData;

        } catch (InvalidAs2MessageException e) {
            log.error("Invalid AS2 message " + e.getMessage(), e);
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(internetHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);

        } catch (MdnRequestException e) {
            log.error("Invalid MDN request: " + e.getMessage());
            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(internetHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);

        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage(), e);
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(internetHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData, e);
        }

    }

    PeppolMessageMetaData collectTransmissionData(As2Message as2Message, SignedMimeMessageInspector SignedMimeMessageInspector) {

        DocumentSniffer documentSniffer = new DocumentSniffer(SignedMimeMessageInspector.getPayload());
        if (!documentSniffer.isSbdhDetected()) {
            throw new IllegalStateException("Payload does not contain Standard Business Document Header");
        }

        // Parses the SBDH and obtains metadata
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = sbdhParser.parse(SignedMimeMessageInspector.getPayload());

        PeppolMessageMetaData peppolMessageMetaData = new PeppolMessageMetaData();

        peppolMessageMetaData.setTransmissionId(as2Message.getTransmissionId());
        peppolMessageMetaData.setMessageId(peppolStandardBusinessHeader.getMessageId().toString());


        peppolMessageMetaData.setSenderId(peppolStandardBusinessHeader.getSenderId());
        peppolMessageMetaData.setRecipientId(peppolStandardBusinessHeader.getRecipientId());
        peppolMessageMetaData.setDocumentTypeIdentifier(peppolStandardBusinessHeader.getDocumentTypeIdentifier());
        peppolMessageMetaData.setProfileTypeIdentifier(peppolStandardBusinessHeader.getProfileTypeIdentifier());

        peppolMessageMetaData.setSendingAccessPointId(new AccessPointIdentifier(as2Message.getAs2From().toString()));
        peppolMessageMetaData.setReceivingAccessPoint(new AccessPointIdentifier(as2Message.getAs2To().toString()));

        // Retrieves the Common Name of the X500Principal, which is used to construct the AccessPointIdentifier for the senders access point
        X500Principal subjectX500Principal = SignedMimeMessageInspector.getSignersX509Certificate().getSubjectX500Principal();
        peppolMessageMetaData.setSendingAccessPointPrincipal(subjectX500Principal);


        return peppolMessageMetaData;
    }


    private As2DispositionNotificationOptions inspectDispositionNotificationOptions(InternetHeaders internetHeaders) throws MdnRequestException {

        String[] headerValue = internetHeaders.getHeader(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName());
        if (headerValue == null || headerValue[0] == null) {
            throw new MdnRequestException("AS2 header '" + As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName() + "' not found in request");
        }

        // Attempts to parseMultipart the Disposition Notification Options
        String value = headerValue[0];
        As2DispositionNotificationOptions as2DispositionNotificationOptions = As2DispositionNotificationOptions.valueOf(value);
        String micAlgorithm = as2DispositionNotificationOptions.getSignedReceiptMicalg().textValue;
        if (!micAlgorithm.equalsIgnoreCase("sha1")) {
            throw new MdnRequestException("Invalid MIC algorithm, only SHA-1 supported:" + micAlgorithm);
        }
        return as2DispositionNotificationOptions;
    }
}