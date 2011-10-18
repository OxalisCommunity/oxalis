/*
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
package eu.peppol.inbound.sml;

import eu.peppol.inbound.util.Log;
import org.busdox.smp.EndpointType;
import org.busdox.smp.ProcessType;
import org.busdox.smp.SignedServiceMetadataType;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
 * The SMLLookup aims to hold the entire processes required for getting a recipient URL endpoint address by
 * using the SML service.
 *
 * @author Dante Malaga(dante@alfa1lab.com)
 *         Jose Gorvenia Narvaez(jose@alfa1lab.com)
 */
public class SmpLookup {

    private static final String ALGORITHM_MD5 = "MD5";
    private static final String ENCODING_GZIP = "gzip";
    private static final String ENCODING_DEFLATE = "deflate";
    public static final String SML_ENDPOINT_ADDRESS = "sml.peppolcentral.org";
    private static SignedServiceMetadataType signedServiceMetadata;

    /**
     * Gets the recipient URL endpoint address given a logical ParticipantID
     * (ParticipantID scheme and ParticipantID value) and a logical DocumentID
     * (DocumentID scheme and String DocumentID value)
     */
    public static String getEndpointAddress(ParticipantIdentifierType participant, DocumentIdentifierType documentId) {

        String content = getDocument(SML_ENDPOINT_ADDRESS, participant, documentId);
        Document document = parseStringtoDocument(content);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SignedServiceMetadataType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<SignedServiceMetadataType> root = unmarshaller.unmarshal(document, SignedServiceMetadataType.class);

            String address = root.getValue().getServiceMetadata().getServiceInformation().getProcessList().getProcess().get(0).getServiceEndpointList().getEndpoint().get(0).getEndpointReference().getAddress().getValue();
            Log.info("Endpoint address from SMP: " + address);
            return address;
        } catch (JAXBException ex) {
            Log.error("JAXB error unmarshal the response from SML", ex);
        }

        return null;
    }

    /**
     * Gets the SignedServiceMetadata String holding the metadata of a given logical ParticipantID and logical
     * DocumentID.
     */
    public static String getDocument(String smlUrl,
                                     ParticipantIdentifierType participantIdentifierType,
                                     DocumentIdentifierType documentIdentifierType) {

        String restUrl = "";
        String scheme = participantIdentifierType.getScheme();
        String value = participantIdentifierType.getValue();

        try {
            String dns = getSmpHostName(smlUrl, scheme, value);

            restUrl = "http://" + dns + "/"
                    + URLEncoder.encode(scheme + "::" + value, "UTF-8")
                    + "/services/"
                    + URLEncoder.encode(documentIdentifierType.getScheme() + "::" + documentIdentifierType.getValue(), "UTF-8");
        } catch (NoSuchAlgorithmException nax) {
            Log.error("Error generation MD5", nax);
        } catch (UnsupportedEncodingException uex) {
            Log.error("Error encoding", uex);
        }

        return getURLContent(restUrl);
    }

    public static String getSmpHostName(String smlUrl, String businessIdScheme, String businessIdValue)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        return "B-" + calculateMD5(businessIdValue.toLowerCase())
                + "." + businessIdScheme
                + "." + smlUrl;
    }

    /**
     * Generates a MD5 hash given a String value.
     *
     * @param value Input String.
     * @return Generated MD5 hash.
     * @throws NoSuchAlgorithmException     Thrown if the MD5 algorithm is not available in this environment.
     * @throws UnsupportedEncodingException Thrown if the iso-8859-1 character encoding is not supported.
     */
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

    /**
     * Gets the content of a given url.
     *
     * @param restUrl URL where the content is allocated.
     * @return URL content.
     */
    public static String getURLContent(String restUrl) {

        InputStream in = null;
        InputStream result = null;
        BufferedReader buffReader = null;

        StringBuilder strBuffer = new StringBuilder();

        try {
            URL url = new URL(restUrl);

            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.connect();

            String encoding = httpConn.getContentEncoding();

            in = httpConn.getInputStream();

            if (encoding != null && encoding.equalsIgnoreCase(ENCODING_GZIP)) {
                result = new GZIPInputStream(in);
            } else if (encoding != null && encoding.equalsIgnoreCase(ENCODING_DEFLATE)) {
                result = new InflaterInputStream(in);
            } else {
                result = in;
            }

            buffReader = new BufferedReader(new InputStreamReader(in));

            String line = null;

            while ((line = buffReader.readLine()) != null) {
                strBuffer.append(line).append("\n");
            }
        } catch (IOException iox) {
            Log.error("", iox);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Log.error("", ex);
                }
            }

            if (result != null) {
                try {
                    result.close();
                } catch (IOException ex) {
                    Log.error("", ex);
                }
            }

            if (buffReader != null) {
                try {
                    buffReader.close();
                } catch (IOException ex) {
                    Log.error("", ex);
                }
            }
        }

        return strBuffer.toString();
    }

    private static SignedServiceMetadataType getEndpointCert(Document xml) {

        SignedServiceMetadataType cert = null;

        try {
            JAXBContext context = JAXBContext.newInstance(SignedServiceMetadataType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<SignedServiceMetadataType> root = unmarshaller.unmarshal(xml, SignedServiceMetadataType.class);
            return root.getValue();
        } catch (JAXBException ex) {
            Log.error("", ex);
        }
        return cert;
    }

    private static String getCertificateReference(ProcessIdentifierType processIdentifierType) {

        String cert = null;

        List<ProcessType> processes = signedServiceMetadata.getServiceMetadata().getServiceInformation().getProcessList().getProcess();
        String scheme = processIdentifierType.getScheme();
        String value = processIdentifierType.getValue();

        for (ProcessType process : processes) {
            if (scheme.equals(process.getProcessIdentifier().getScheme())
                    && value.equals(process.getProcessIdentifier().getValue())) {
                EndpointType enpointType = process.getServiceEndpointList().getEndpoint().get(0);
                cert = enpointType.getCertificate();
                break;
            }
        }

        Log.info("Endpoint Certificate: \n" + cert);
        return cert;
    }

    public static X509Certificate getEndpointCertificate(
            ParticipantIdentifierType participantIdentifierType,
            DocumentIdentifierType documentIdentifierType,
            ProcessIdentifierType processIdentifierType) {

        String content = getDocument(SML_ENDPOINT_ADDRESS, participantIdentifierType, documentIdentifierType);
        Document document = parseStringtoDocument(content);
        signedServiceMetadata = getEndpointCert(document);
        String endpointCertificate =
                "-----BEGIN CERTIFICATE-----\n" +
                        getCertificateReference(processIdentifierType) +
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

    /**
     * Transforms the given String into a org.w3c.dom.Document object.
     *
     * @param content String which will be transformed.
     * @return Parsed Document.
     */
    private static Document parseStringtoDocument(String content) {

        Document document = null;

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            document = docBuilder.parse(new InputSource(new StringReader(content)));
        } catch (ParserConfigurationException ex) {
            Log.error("", ex);
        } catch (IOException iox) {
            Log.error("", iox);
        } catch (SAXException sax) {
            Log.error("", sax);
        }

        return document;
    }
}
