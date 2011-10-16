package eu.peppol.inbound.smp;

import eu.peppol.inbound.sml.SmpLookup;
import eu.peppol.inbound.util.TestBase;
import eu.peppol.outbound.util.Constants;
import org.testng.annotations.Test;
import org.w3._2009._02.ws_tra.DocumentIdentifierType;
import org.w3._2009._02.ws_tra.ParticipantIdentifierType;
import org.w3._2009._02.ws_tra.ProcessIdentifierType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.testng.Assert.*;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 12:33:13 PM
 */
@Test
public class SmpTest extends TestBase {

    public void test01() throws Throwable {
        try {

            DocumentIdentifierType documentId = Constants.getInvoiceDocumentIdentifier();
            ProcessIdentifierType processId = Constants.getInvoiceProcessIdentifier();
            ParticipantIdentifierType recipient = Constants.getParticipantIdentifier("9902:DK28158815");

            String endpointAddress = SmpLookup.getEndpointAddress(recipient, documentId);
            assertEquals(endpointAddress, "https://start-ap.alfa1lab.com:443/accesspointService");

            String endpointCertificate = SmpLookup.getEndpointCertificate(recipient, documentId, processId);

            endpointCertificate =
                    "-----BEGIN CERTIFICATE-----\n" +
                            endpointCertificate +
                            "\n-----END CERTIFICATE-----";
            System.out.println("Metadata Certificate: \n" + endpointCertificate);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new ByteArrayInputStream(endpointCertificate.getBytes());
            X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);

            System.out.println("Metadata Certificate - Serial Number: " + x509Certificate.getSerialNumber());


        } catch (Throwable t) {
            signal(t);
        }
    }
}
