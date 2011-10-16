/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.peppol.inbound.server;

import eu.peppol.inbound.metadata.MessageMetadata;
import eu.peppol.inbound.sml.SmpLookup;
import eu.peppol.inbound.soap.handler.SOAPInboundHandler;
import eu.peppol.inbound.transport.ReceiverChannel;
import eu.peppol.inbound.util.Log;
import eu.peppol.outbound.client.accesspointClient;
import eu.peppol.outbound.soap.SOAPHeaderObject;
import eu.peppol.start.util.Configuration;
import eu.peppol.start.util.KeystoreManager;
import org.w3._2009._02.ws_tra.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.xml.ws.Action;
import javax.xml.ws.BindingType;
import javax.xml.ws.FaultAction;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * WebService implementation.
 *
 * @author Jose Gorvenia Narvaez(jose@alfa1lab.com) Dante
 *         Malaga(dante@alfa1lab.com)
 */
@SuppressWarnings({"UnusedDeclaration"})
@WebService(serviceName = "accesspointService", portName = "ResourceBindingPort", endpointInterface = "org.w3._2009._02.ws_tra.Resource", targetNamespace = "http://www.w3.org/2009/02/ws-tra", wsdlLocation = "WEB-INF/wsdl/accesspointService/wsdl_v1.5.wsdl")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
@HandlerChain(file = "soap-handlers.xml")
@Addressing
public class accesspointService {

    public static final String SERVICE_NAME = "accesspointService";
    private static final String KEY_PATH = "keystore";
    private static final String KEY_PASS = "keystore.password";

    @javax.annotation.Resource
    private static WebServiceContext webServiceContext;

    public GetResponse get(Get body) {
        throw new UnsupportedOperationException();
    }

    public PutResponse put(Put body) {
        throw new UnsupportedOperationException();
    }

    public DeleteResponse delete(Delete body) {
        throw new UnsupportedOperationException();
    }

    @Action(input = "http://www.w3.org/2009/02/ws-tra/Create", output = "http://www.w3.org/2009/02/ws-tra/CreateResponse", fault = {@FaultAction(className = org.w3._2009._02.ws_tra.FaultMessage.class, value = "http://busdox.org/2010/02/channel/fault")})
    public CreateResponse create(Create body) throws FaultMessage, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, IOException, KeyStoreException {

        Log.info("accesspointService invoked");
        SOAPHeaderObject soapHeader = SOAPInboundHandler.soapHeader;
        MessageMetadata metadata = new MessageMetadata(soapHeader);

        String senderAPUrl = getOwnUrl();
        Log.info("Our endpoint: " + senderAPUrl);

        String recipientAPUrl = getAccessPointURL(senderAPUrl, metadata);
        // String recipientAPUrl = getOwnUrl();
        Log.info("Recipient endpoint: " + senderAPUrl);

        // String certEntry = getAccessPointCert(metadata);
        // Enable this code when you have a working SMP (SOC 13-oct-11)
        /*
           * Log.debug("Metadata Certificate: \n" + certEntry);
           *
           * byte[] certEntryBytes = certEntry.getBytes(); InputStream in = new
           * ByteArrayInputStream(certEntryBytes); CertificateFactory certFactory
           * = CertificateFactory.getInstance(CERT_X509); X509Certificate metaCert
           * = (X509Certificate) certFactory.generateCertificate(in);
           *
           * Log.debug("Metadata Certificate - Serial Number: " +
           * metaCert.getSerialNumber());
           */
        Configuration configuration = Configuration.getInstance();
        String keystorePath = configuration.getProperty(KEY_PATH);
        String keystorePass = configuration.getProperty(KEY_PASS);

        // X509Certificate oursCert =
        // SAMLCallbackHandler.getCertificate(keystorePath, keystorePass);
        X509Certificate oursCert = new KeystoreManager().getOurCertificate();
        X509Certificate metaCert = oursCert;

        // Checking the certificates first, will cause routing not to work (SOC 13-oct-11)
        if (isTheSameCert(metaCert, oursCert)) {
            // TODO: This is not correct. We should check the URL's first!

            if (isTheSame(recipientAPUrl, senderAPUrl)) {

                Log.info("Sender Access Point and Receiver Access Point are the same");
                Log.info("This is a local request - storage directly "
                        + metadata.getRecipient().getValue());
                deliverLocally(metadata, body);

            } else {

                Log.info("Sender Access Point and Receiver Access Point are different");
                Log.info("This is a request for a remote Access Point: "
                        + recipientAPUrl);
                deliverRemotely(metadata, body, recipientAPUrl);
            }
        } else {

            Log.error("Metadata Certificate does not match Access Point Certificate");

        }

        Log.info("Done");
        return new CreateResponse();
    }

    public String getOwnUrl() {

        ServletRequest servletRequest = (ServletRequest) webServiceContext
                .getMessageContext().get(MessageContext.SERVLET_REQUEST);

        String contextPath = ((ServletContext) webServiceContext
                .getMessageContext().get(MessageContext.SERVLET_CONTEXT))
                .getContextPath();
        String thisAPUrl = servletRequest.getScheme() + "://"
                + servletRequest.getServerName() + ":"
                + servletRequest.getLocalPort() + contextPath + '/';

        return thisAPUrl + SERVICE_NAME;
    }

    public String getAccessPointURL(String senderAPUrl, MessageMetadata metadata) {
        boolean isSMPEnabled = false;  // Should we use the SMP or not.

        if (!isSMPEnabled) {
            String recipientId = metadata.getRecipient().getValue();
            boolean isRecipientIdEven = (Integer.parseInt(recipientId
                    .substring(recipientId.length() - 2, recipientId.length())) % 2 == 0);

            Log.info("Recipient ID: " + recipientId + " (even="
                    + isRecipientIdEven + ")");

            if (isRecipientIdEven) {
                return "https://localhost:8443/oxalis2/accesspointService";
            } else {
                return getOwnUrl();
            }
        } else {
            return SmpLookup.getEndpointAddress(metadata.getRecipient(), metadata.getDocumentIdentifierType());
        }
    }

    public String getAccessPointCert(MessageMetadata metadata) {
        return SmpLookup.getEndpointCertificate(
                metadata.getRecipient(),
                metadata.getDocumentIdentifierType(),
                metadata.getProcessIdentifierType());
    }

    private boolean isTheSame(String recipientAPUrl, String senderAPUrl) {
        return recipientAPUrl.indexOf(senderAPUrl) >= 0;
    }

    private boolean isTheSameCert(X509Certificate meta, X509Certificate ours) {
        return ours.getSerialNumber().toString()
                .equals(meta.getSerialNumber().toString());
    }

    public void deliverRemotely(MessageMetadata metadata, Create body, String recipientApUrl) {

        accesspointClient client = new accesspointClient();
        client.enableSoapLogging(false);
        client.send(recipientApUrl, metadata.getSoapHeader(), body);
    }

    public void deliverLocally(MessageMetadata metadata, Create body) {

        String channelId = metadata.getRecipient().getValue();

        if (channelId != null) {
            metadata.setChannelId(channelId);
        }

        List<Object> objects = body.getAny();

        if (objects != null && objects.size() == 1) {
            Element element = (Element) objects.iterator().next();
            Document businessDocument = element.getOwnerDocument();
            new ReceiverChannel().deliverMessage(metadata, businessDocument);
        }
    }
}
