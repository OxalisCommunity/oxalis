package network.oxalis.ext.testbed.v1;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import network.oxalis.api.evidence.EvidenceFactory;
import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.api.outbound.Transmitter;
import network.oxalis.commons.util.OxalisVersion;
import network.oxalis.ext.testbed.v1.jaxb.InformationType;
import network.oxalis.ext.testbed.v1.jaxb.OutboundResponseType;
import network.oxalis.ext.testbed.v1.jaxb.OutboundType;
import network.oxalis.outbound.transmission.TransmissionRequestFactory;
import network.oxalis.vefa.peppol.common.model.TransportProfile;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author erlend
 */
@Singleton
public class TestbedServlet extends HttpServlet {

    @Inject
    private Provider<TransmissionRequestFactory> transmissionRequestFactory;

    @Inject
    private Provider<Transmitter> transmitter;

    @Inject
    @Named("rem")
    private EvidenceFactory evidenceFactory;

    @Inject
    @Named("prioritized")
    private List<TransportProfile> transportProfiles;

    @Inject
    private X509Certificate certificate;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            InformationType informationType = new InformationType();
            informationType.setName("Oxalis");
            informationType.setVersion(OxalisVersion.getVersion());
            informationType.setApiVersion("1.0");
            informationType.getTransportProfile().addAll(transportProfiles.stream()
                    .map(TransportProfile::getIdentifier)
                    .collect(Collectors.toList()));
            informationType.setCertificate(certificate.getEncoded());

            resp.addHeader("Content-Type", "application/xml;charset=UTF-8");
            TestbedJaxb.marshaller().marshal(
                    TestbedJaxb.OBJECT_FACTORY.createInformation(informationType),
                    resp.getWriter());
        } catch (JAXBException | CertificateEncodingException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            OutboundType outbound = TestbedJaxb.unmarshaller()
                    .unmarshal(new StreamSource(req.getInputStream()), OutboundType.class)
                    .getValue();

            TransmissionMessage transmissionMessage = transmissionRequestFactory.get()
                    .newInstance(new ByteArrayInputStream(outbound.getPayload()));

            TransmissionResponse transmissionResponse;
            if (outbound.getDestination() == null) {
                transmissionResponse = transmitter.get()
                        .transmit(transmissionMessage);
            } else {
                transmissionResponse = transmitter.get()
                        .transmit(new TestbedTransmissionRequest(transmissionMessage, outbound.getDestination()));
            }

            InputStream evidenceInputStream = evidenceFactory.write(transmissionResponse);

            OutboundResponseType response = new OutboundResponseType();
            response.setReceipt(ByteStreams.toByteArray(evidenceInputStream));

            resp.addHeader("Content-Type", "application/xml;charset=UTF-8");
            TestbedJaxb.marshaller().marshal(
                    TestbedJaxb.OBJECT_FACTORY.createOutboundResponse(response),
                    resp.getWriter());
        } catch (JAXBException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (Exception e) {
            try {
                OutboundResponseType response = new OutboundResponseType();
                response.setError(e.getMessage());

                resp.addHeader("Content-Type", "application/xml;charset=UTF-8");
                TestbedJaxb.marshaller().marshal(
                        TestbedJaxb.OBJECT_FACTORY.createOutboundResponse(response),
                        resp.getWriter());
            } catch (JAXBException ex) {
                throw new ServletException(ex.getMessage(), ex);
            }
        }
    }
}
