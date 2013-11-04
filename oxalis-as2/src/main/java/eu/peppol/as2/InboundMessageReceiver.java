package eu.peppol.as2;

import eu.peppol.PeppolMessageMetaData;
import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.sbdh.SbdhMessageRepository;
import eu.peppol.sbdh.SbdhParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.Security;
import java.util.Map;

/**
 * @author steinar
 *         Date: 20.10.13
 *         Time: 10:45
 */
public class InboundMessageReceiver {

    public static final Logger log = LoggerFactory.getLogger(InboundMessageReceiver.class);

    public InboundMessageReceiver() {
        // Gives us access to BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

    }

    /**
     * Receives an AS2 Message in the form of a map of headers together with the payload, which is made available
     * in an input stream
     *
     *
     * @param mapOfHeaders supplies the AS2 headers
     * @param inputStream  supplies the actual data
     * @param sbdhMessageRepository
     * @return MDN object if everything is ok.
     * @throws ErrorWithMdnException if validation fails due to syntactic, semantic or other reasons.
     */
    public MdnData receive(Map<String, String> mapOfHeaders, InputStream inputStream, SbdhMessageRepository sbdhMessageRepository) throws ErrorWithMdnException {

        try {
            log.info("Receiving message ..");
            // Inspects the eu.peppol.as2.As2Header.DISPOSITION_NOTIFICATION_OPTIONS
            inspectDispositionNotificationOptions(mapOfHeaders);

            log.info("Message contains valid Disposition-notification-options, now creating internal AS2 message...");
            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(mapOfHeaders, inputStream);

            log.info("Validating AS2 Message: " + as2Message);

            // Validates the message headers according to the PEPPOL rules
            // Performs semantic validation
            SMimeMessageInspector SMimeMessageInspector = As2MessageInspector.validate(as2Message);

            // Persists the payload
            InputStream payloadInputStream = SMimeMessageInspector.getPayload();

            PeppolMessageMetaData transmissionData = collectTransmissionData(as2Message, SMimeMessageInspector);

            log.info("Persisting AS2 Message ....");
            sbdhMessageRepository.persist(transmissionData, payloadInputStream);

//                smimeMessageInspector.getMimeMessage().writeTo(System.out);


            // Calculates the MIC for the payload
            As2DispositionNotificationOptions.Parameter signedReceiptMicalg = as2Message.getDispositionNotificationOptions().getSignedReceiptMicalg();
            String micAlgorithmName = signedReceiptMicalg.getTextValue();
            Mic mic = SMimeMessageInspector.calculateMic(micAlgorithmName);

            // Creates the MDN to be returned
            MdnData mdnData = MdnData.Builder.buildProcessedOK(mapOfHeaders, mic);
            log.info("Message received OK, MDN returned: " + mdnData);
            return mdnData;
        } catch (InvalidAs2MessageException e) {
            log.error("Invalid AS2 message " + e.getMessage(), e);
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(mapOfHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);
        } catch (MdnRequestException e) {
            log.error("Invalid MDN request: " + e.getMessage());
            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(mapOfHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage(), e);
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(mapOfHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData, e);
        }

    }

    PeppolMessageMetaData collectTransmissionData(As2Message as2Message, SMimeMessageInspector SMimeMessageInspector) {

        SbdhParser sbdhParser = new SbdhParser();

        // Parses the SBDH and obtains metadata
        PeppolStandardBusinessHeader peppolStandardBusinessHeader = sbdhParser.parse(SMimeMessageInspector.getPayload());

        PeppolMessageMetaData peppolMessageMetaData = new PeppolMessageMetaData();

        peppolMessageMetaData.setMessageId(peppolStandardBusinessHeader.getMessageId().toString());
        peppolMessageMetaData.setSenderId(peppolStandardBusinessHeader.getSenderId());
        peppolMessageMetaData.setRecipientId(peppolStandardBusinessHeader.getRecipientId());
        peppolMessageMetaData.setDocumentTypeIdentifier(peppolStandardBusinessHeader.getDocumentTypeIdentifier().toString());
        peppolMessageMetaData.setProfileTypeIdentifier(peppolStandardBusinessHeader.getProfileTypeIdentifier().toString());

        peppolMessageMetaData.setSendingAccessPoint(as2Message.getAs2From().toString());
        peppolMessageMetaData.setReceivingAccessPoint(as2Message.getAs2To().toString());
        peppolMessageMetaData.setSendingAccessPointDistinguishedName(SMimeMessageInspector.getSignersX509Certificate().getSubjectDN().getName());
        peppolMessageMetaData.setAs2MessageId(as2Message.getMessageId());

        return peppolMessageMetaData;
    }


    private As2DispositionNotificationOptions inspectDispositionNotificationOptions(Map<String, String> map) throws MdnRequestException {

        String headerValue = map.get(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName());
        if (headerValue == null) {
            throw new MdnRequestException("AS2 header '" + As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName() + "' not found in request");
        }

        // Attempts to parse the Disposition Notification Options
        As2DispositionNotificationOptions as2DispositionNotificationOptions = As2DispositionNotificationOptions.valueOf(headerValue);
        String micAlgorithm = as2DispositionNotificationOptions.getSignedReceiptMicalg().textValue;
        if (!micAlgorithm.equalsIgnoreCase("sha1")) {
            throw new MdnRequestException("Invalid MIC algorithm, only SHA-1 supported:" + micAlgorithm);
        }
        return as2DispositionNotificationOptions;
    }
}