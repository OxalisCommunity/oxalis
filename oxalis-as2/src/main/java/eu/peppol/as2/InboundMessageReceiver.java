package eu.peppol.as2;

import eu.peppol.PeppolMessageInformation;
import eu.peppol.sbdh.SbdhMessageRepository;
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
            // Inspects the eu.peppol.as2.As2Header.DISPOSITION_NOTIFICATION_OPTIONS
            inspectDispositionNotificationOptions(mapOfHeaders);

            // Transforms the input data into a proper As2Message
            As2Message as2Message = As2MessageFactory.createAs2MessageFrom(mapOfHeaders, inputStream);

            // Validates the message headers according to the PEPPOL rules
            // Performs semantic validation
            SmimeMessageInspector smimeMessageInspector = As2MessageInspector.validate(as2Message);

            // Persists the payload
            InputStream payloadInputStream = smimeMessageInspector.getPayload();

            PeppolMessageInformation transmissionData = collectTransmissionData(as2Message, smimeMessageInspector);

            sbdhMessageRepository.persist(transmissionData, payloadInputStream);

//                smimeMessageInspector.getMimeMessage().writeTo(System.out);


            // Calculates the MIC for the payload
            String micAlgorithmName = as2Message.getDispositionNotificationOptions().getSignedReceiptMicalg().getTextValue();
            Mic mic = smimeMessageInspector.calculateMic(micAlgorithmName);

            // Creates the MDN to be returned
            MdnData mdnData = MdnData.Builder.buildProcessedOK(mapOfHeaders, mic);

            return mdnData;
        } catch (InvalidAs2MessageException e) {
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(mapOfHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);
        } catch (MdnRequestException e) {
            MdnData mdnData = MdnData.Builder.buildFailureFromHeaders(mapOfHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);
        } catch (Exception e) {
            MdnData mdnData = MdnData.Builder.buildProcessingErrorFromHeaders(mapOfHeaders, e.getMessage());
            throw new ErrorWithMdnException(mdnData);
        }

    }

    PeppolMessageInformation collectTransmissionData(As2Message as2Message, SmimeMessageInspector smimeMessageInspector) {
        SbdhParser sbdhParser = new SbdhParser();
        PeppolMessageInformation peppolMessageInformation = sbdhParser.parse(smimeMessageInspector.getPayload());
        peppolMessageInformation.setSendingAccessPoint(as2Message.getAs2From().toString());
        peppolMessageInformation.setReceivingAccessPoint(as2Message.getAs2To().toString());
        peppolMessageInformation.setSendingAccessPointDistinguishedName(smimeMessageInspector.getSignersX509Certificate().getSubjectDN().getName());

        return peppolMessageInformation;
    }


    private As2DispositionNotificationOptions inspectDispositionNotificationOptions(Map<String, String> map) throws MdnRequestException {

        String headerValue = map.get(As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName());
        if (headerValue == null) {
            throw new MdnRequestException("AS2 header " + As2Header.DISPOSITION_NOTIFICATION_OPTIONS.getHttpHeaderName() + " not found in request");
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