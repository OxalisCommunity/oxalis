package eu.peppol.outbound.smp;

import eu.peppol.outbound.util.Log;
import org.busdox.smp.EndpointType;
import org.busdox.smp.ProcessType;
import org.busdox.smp.SignedServiceMetadataType;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:01:53 AM
 */
public class SmpLookupManager {

    private static final String ALGORITHM_MD5 = "MD5";
    private static final String ENCODING_GZIP = "gzip";
    private static final String ENCODING_DEFLATE = "deflate";
    public static final String SML_ENDPOINT_ADDRESS = "sml.peppolcentral.org";

    private static String calculateMD5(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM_MD5);
        messageDigest.reset();
        messageDigest.update(value.getBytes("iso-8859-1"), 0, value.length());
        byte[] digest = messageDigest.digest();
        StringBuilder sb = new StringBuilder();

        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);

            if (hex.length() == 1) {
                sb.append('0');
            }

            sb.append(hex);
        }

        return sb.toString();
    }

    private static String getCertificateReference(ProcessIdentifierType processIdentifierType, SignedServiceMetadataType signedServiceMetadata) {

        String certificate = null;

        List<ProcessType> processTypes =
                signedServiceMetadata.getServiceMetadata().getServiceInformation().getProcessList().getProcess();

        String scheme = processIdentifierType.getScheme();
        String value = processIdentifierType.getValue();

        for (ProcessType process : processTypes) {
            if (scheme.equals(process.getProcessIdentifier().getScheme())
                    && value.equals(process.getProcessIdentifier().getValue())) {
                EndpointType endpointType = process.getServiceEndpointList().getEndpoint().get(0);
                certificate = endpointType.getCertificate();
                break;
            }
        }

        Log.info("Endpoint Certificate: \n" + certificate);
        return certificate;
    }

    /**
     * Gets the SignedServiceMetadata String holding the metadata of a given logical ParticipantID and logical
     * DocumentID.
     */
    private static String getDocument(String smlUrl,
                                     ParticipantIdentifierType participantIdentifierType,
                                     DocumentIdentifierType documentIdentifierType) {

        String restUrl;
        String scheme = participantIdentifierType.getScheme();
        String value = participantIdentifierType.getValue();

        try {
            String dns = getSmpHostName(smlUrl, scheme, value);

            restUrl = "http://" + dns + "/"
                    + URLEncoder.encode(scheme + "::" + value, "UTF-8")
                    + "/services/"
                    + URLEncoder.encode(documentIdentifierType.getScheme() + "::" + documentIdentifierType.getValue(), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Problem constructing SMP URL", e);
        }

        return getURLContent(restUrl);
    }

    public URL getEndpointAddress(ParticipantIdentifierType participant, DocumentIdentifierType documentId) {

        String content = getDocument(SML_ENDPOINT_ADDRESS, participant, documentId);
        Document document = parseStringtoDocument(content);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SignedServiceMetadataType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<SignedServiceMetadataType> root = unmarshaller.unmarshal(document, SignedServiceMetadataType.class);

            String address = root
                    .getValue()
                    .getServiceMetadata()
                    .getServiceInformation()
                    .getProcessList()
                    .getProcess()
                    .get(0)
                    .getServiceEndpointList()
                    .getEndpoint()
                    .get(0)
                    .getEndpointReference()
                    .getAddress()
                    .getValue();

            Log.info("Endpoint address from SMP: " + address);
            return new URL(address);

        } catch (Exception e) {
            throw new RuntimeException("Failed to unmarshal SMP response", e);
        }
    }

    public X509Certificate getEndpointCertificate(
            ParticipantIdentifierType participantIdentifierType,
            DocumentIdentifierType documentIdentifierType,
            ProcessIdentifierType processIdentifierType) {

        String content = getDocument(SML_ENDPOINT_ADDRESS, participantIdentifierType, documentIdentifierType);
        Document document = parseStringtoDocument(content);
        SignedServiceMetadataType signedServiceMetadata = getEndpointCertificate(document);
        String endpointCertificate =
                "-----BEGIN CERTIFICATE-----\n" +
                        getCertificateReference(processIdentifierType, signedServiceMetadata) +
                        "\n-----END CERTIFICATE-----";
        X509Certificate certificate;

        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new ByteArrayInputStream(endpointCertificate.getBytes());
            certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        } catch (CertificateException e) {
            throw new RuntimeException(
                    "Failed to get certificate for " +
                            participantIdentifierType.getScheme() + ":" +
                            participantIdentifierType.getValue());
        }

        return certificate;
    }

    private static SignedServiceMetadataType getEndpointCertificate(Document xml) {

        try {

            JAXBContext context = JAXBContext.newInstance(SignedServiceMetadataType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<SignedServiceMetadataType> root = unmarshaller.unmarshal(xml, SignedServiceMetadataType.class);
            return root.getValue();

        } catch (Exception e) {
            throw new RuntimeException("Problem getting endpoint certificate", e);
        }
    }

    private static String getSmpHostName(String smlUrl, String businessIdScheme, String businessIdValue)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        return "B-" + calculateMD5(businessIdValue.toLowerCase())
                + "." + businessIdScheme
                + "." + smlUrl;
    }

    /**
     * Gets the content of a given url.
     *
     * @param restUrl URL where the content is allocated.
     * @return URL content.
     */
    private static String getURLContent(String restUrl) {

        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(restUrl);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            String encoding = httpURLConnection.getContentEncoding();
            InputStream in = httpURLConnection.getInputStream();
            InputStream result;

            if (encoding != null && encoding.equalsIgnoreCase(ENCODING_GZIP)) {
                result = new GZIPInputStream(in);
            } else if (encoding != null && encoding.equalsIgnoreCase(ENCODING_DEFLATE)) {
                result = new InflaterInputStream(in);
            } else {
                result = in;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(result));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

        } catch (Exception e) {
            throw new RuntimeException("Problem reading SMP data", e);
        } finally {
            try {
                //noinspection ConstantConditions
                bufferedReader.close();
            } catch (Exception e) {
            }
        }

        return sb.toString();
    }

    private static Document parseStringtoDocument(String content) {

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(content)));

        } catch (Exception e) {
            throw new RuntimeException("Problem parsing XML document", e);
        }
    }
}
