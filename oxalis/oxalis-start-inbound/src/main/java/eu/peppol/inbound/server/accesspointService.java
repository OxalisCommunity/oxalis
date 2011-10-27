package eu.peppol.inbound.server;

import eu.peppol.inbound.metadata.MessageMetadata;
import eu.peppol.inbound.soap.handler.SOAPInboundHandler;
import eu.peppol.inbound.transport.ReceiverChannel;
import eu.peppol.inbound.util.Log;
import eu.peppol.outbound.smp.SmpLookupManager;
import eu.peppol.outbound.soap.SoapHeader;
import eu.peppol.start.util.KeystoreManager;
import org.w3._2009._02.ws_tra.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.BindingType;
import javax.xml.ws.FaultAction;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@WebService(serviceName = "accesspointService", portName = "ResourceBindingPort", endpointInterface = "org.w3._2009._02.ws_tra.Resource", targetNamespace = "http://www.w3.org/2009/02/ws-tra", wsdlLocation = "WEB-INF/wsdl/accesspointService/wsdl_v2.0.wsdl")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
@HandlerChain(file = "soap-handlers.xml")
@Addressing
public class accesspointService {

    @javax.annotation.Resource
    private static WebServiceContext webServiceContext;

    @Action(input = "http://www.w3.org/2009/02/ws-tra/Create",
            output = "http://www.w3.org/2009/02/ws-tra/CreateResponse",
            fault = {@FaultAction(className = org.w3._2009._02.ws_tra.FaultMessage.class,
                    value = "http://busdox.org/2010/02/channel/fault")})
    public CreateResponse create(Create body) throws FaultMessage, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, IOException, KeyStoreException {

        KeystoreManager keystoreManager = new KeystoreManager();
        SmpLookupManager smpLookupManager = new SmpLookupManager();

        SoapHeader soapHeader = SOAPInboundHandler.SOAP_HEADER;
        MessageMetadata messageMetadata = new MessageMetadata(soapHeader);
        Log.info("Received inbound message for " + soapHeader.getRecipient());

        try {
            X509Certificate recipientCertificate = smpLookupManager.getEndpointCertificate(
                    messageMetadata.getRecipient(),
                    messageMetadata.getDocumentIdentifierType());

            if (keystoreManager.isOurCertificate(recipientCertificate)) {
                Log.info("SMP lookup OK");
            } else {
                Log.info("SMP lookup indicates that document was sent to the wrong access point");
                throw new FaultMessage("This message was sent to the wrong Access Point", new StartException());
            }
        } catch (Exception e) {
            Log.info("SMP lookup fails, we assume the message is for us");
        }

        storeMessage(messageMetadata, body);
        return new CreateResponse();
    }

    public void storeMessage(MessageMetadata metadata, Create body) {

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

    public GetResponse get(Get body) {
        throw new UnsupportedOperationException();
    }

    public PutResponse put(Put body) {
        throw new UnsupportedOperationException();
    }

    public DeleteResponse delete(Delete body) {
        throw new UnsupportedOperationException();
    }
}
