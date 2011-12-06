package eu.peppol.outbound.smp;

import eu.peppol.outbound.util.Log;
import eu.peppol.outbound.util.Util;
import eu.peppol.start.identifier.DocumentId;
import eu.peppol.start.identifier.ParticipantId;
import org.busdox.smp.EndpointType;
import org.busdox.smp.SignedServiceMetadataType;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:01:53 AM
 */
public class SmpLookupManager {

    public URL getEndpointAddress(ParticipantId participant, DocumentId documentId) {

        EndpointType endpointType = getEndpointType(participant, documentId);
        String address = endpointType.getEndpointReference().getAddress().getValue();
        Log.info("Found endpoint address for " + participant.stringValue() + " from SMP: " + address);

        try {
            return new URL(address);
        } catch (Exception e) {
            throw new RuntimeException("SMP returned invalid URL", e);
        }
    }

    public X509Certificate getEndpointCertificate(ParticipantId participant, DocumentId documentId) {

        try {
            String body = getEndpointType(participant, documentId).getCertificate();
            String endpointCertificate = "-----BEGIN CERTIFICATE-----\n" + body + "\n-----END CERTIFICATE-----";
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(endpointCertificate.getBytes()));

        } catch (CertificateException e) {
            throw new RuntimeException("Failed to get certificate from SMP for " + ParticipantId.getScheme() + ":" + participant.stringValue());
        }
    }

    private EndpointType getEndpointType(ParticipantId participant, DocumentId documentId) {

        try {
            URL smpUrl = getSmpUrl(participant, documentId);
            Log.debug("Constructed SMP url: " + smpUrl.toExternalForm().replaceAll("%3A", ":").replaceAll("%23", "#"));
            InputSource smpContents = Util.getUrlContent(smpUrl);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(smpContents);
            Unmarshaller unmarshaller = JAXBContext.newInstance(SignedServiceMetadataType.class).createUnmarshaller();

            SignedServiceMetadataType serviceMetadata =
                    unmarshaller.unmarshal(document, SignedServiceMetadataType.class).getValue();

            return serviceMetadata
                    .getServiceMetadata()
                    .getServiceInformation()
                    .getProcessList()
                    .getProcess()
                    .get(0)
                    .getServiceEndpointList()
                    .getEndpoint()
                    .get(0);

        } catch (Exception e) {
            throw new RuntimeException("Problem with SMP lookup", e);
        }
    }

    private static URL getSmpUrl(ParticipantId participantId, DocumentId documentId) throws Exception {

        String scheme = ParticipantId.getScheme();
        String value = participantId.stringValue();
        String hostname = "B-" + Util.calculateMD5(value.toLowerCase()) + "." + scheme + "." + "sml.peppolcentral.org";
        String encodedParticipant = URLEncoder.encode(scheme + "::" + value, "UTF-8");
        String encodedDocumentId = URLEncoder.encode(DocumentId.getScheme() + "::" + documentId.stringValue(), "UTF-8");

        return new URL("http://" + hostname + "/" + encodedParticipant + "/services/" + encodedDocumentId);
    }
}
