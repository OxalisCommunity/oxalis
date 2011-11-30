package eu.peppol.inbound.server;

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.developer.JAXWSProperties;
import eu.peppol.inbound.soap.SoapHeaderParser;
import eu.peppol.inbound.transport.FileBasedTransportChannel;
import eu.peppol.inbound.util.Log;
import eu.peppol.outbound.smp.SmpLookupManager;
import eu.peppol.outbound.soap.SoapHeader;
import eu.peppol.start.persistence.MessageRepositoryFactory;
import eu.peppol.start.util.IdentifierName;
import eu.peppol.start.util.KeystoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@WebService(serviceName = "accesspointService", portName = "ResourceBindingPort", endpointInterface = "org.w3._2009._02.ws_tra.Resource", targetNamespace = "http://www.w3.org/2009/02/ws-tra", wsdlLocation = "WEB-INF/wsdl/accesspointService/wsdl_v2.0.wsdl")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
@HandlerChain(file = "soap-handlers.xml")
@Addressing
public class accesspointService {

    @javax.annotation.Resource
    private WebServiceContext webServiceContext;

    private static Logger logger = LoggerFactory.getLogger(accesspointService.class);

    @Action(input = "http://www.w3.org/2009/02/ws-tra/Create",
            output = "http://www.w3.org/2009/02/ws-tra/CreateResponse",
            fault = {@FaultAction(className = org.w3._2009._02.ws_tra.FaultMessage.class,
                    value = "http://busdox.org/2010/02/channel/fault")})
    public CreateResponse create(Create body) throws FaultMessage, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, IOException, KeyStoreException {

        try {

            SoapHeader soapHeader = fetchPeppolSoapHeaderFromThisRequest();
            Log.info("Received PEPPOL SOAP Header:" + soapHeader);

            // Injects current context into SLF4J Mapped Diagnostic Context
            setUpSlf4JMDC(soapHeader);

            Log.info("Received inbound document from " + soapHeader.getSenderIdentifier().getValue() + " for " + soapHeader.getRecipient());

            verifyThatThisDocumentIsForUs(soapHeader);

            Document document = ((Element) body.getAny().get(0)).getOwnerDocument();

            FileBasedTransportChannel transportChannel = new FileBasedTransportChannel();

            transportChannel.saveDocument(soapHeader, document);
            CreateResponse createResponse = new CreateResponse();
            Log.info("Inbound document successfully handled");

            // Invokes the message persistence
            persistMessage(soapHeader, document);

            return createResponse;

        } catch (Exception e) {
            Log.error("Problem while handling inbound document", e);
            throw new FaultMessage("Unexpected error in document handling", new StartException());
        } finally {
            MDC.clear();
        }
    }

    /**
     * Extracts meta data from the SOAP Header, i.e. the routing information and invokes a pluggable
     * message persistence in order to allow for storage of the meta data and the message itself.
     *
     * @param soapHeader PEPPOL Soap header, i.e. only the properties of interest to us
     * @param document the XML document.
     */
    void persistMessage(SoapHeader soapHeader, Document document) {

        Map<IdentifierName, String> map = new HashMap<IdentifierName, String>();
        map.put(IdentifierName.MESSAGE_ID, soapHeader.getMessageIdentifier());
        map.put(IdentifierName.CHANNEL_ID, soapHeader.getChannelIdentifier());
        map.put(IdentifierName.SENDER_ID, soapHeader.getSenderIdentifier().getValue());
        map.put(IdentifierName.RECIPIENT_ID, soapHeader.getRecipientIdentifier().getValue());
        map.put(IdentifierName.DOCUMENT_ID, soapHeader.getDocumentIdentifier().getValue());
        map.put(IdentifierName.PROCESS_ID, soapHeader.getProcessIdentifier().getValue());

        // Invokes whatever has been configured in META-INF/services/.....
        try {
            MessageRepositoryFactory.getInstance().saveMessage(map, document);
        } catch (Exception e) {
            Log.error("Unable to persist: " + e, e);
        }
    }

    SoapHeader fetchPeppolSoapHeaderFromThisRequest() {

        // Grabs the list of headers from the SOAP message
        HeaderList headerList = (HeaderList) webServiceContext.getMessageContext().get(JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);

        // Retrieves the headers we are interested in
        SoapHeader soapHeader = SoapHeaderParser.fetchPeppolSoapHeader(headerList);

        return soapHeader;
    }

    void setUpSlf4JMDC(SoapHeader soapHeader) {
        String messageIdentifier = soapHeader.getMessageIdentifier();
        MDC.put("msgId", messageIdentifier);
        MDC.put("senderId", soapHeader.getSenderIdentifier().getValue());
        MDC.put("channelId", soapHeader.getChannelIdentifier());
    }

    private void verifyThatThisDocumentIsForUs(SoapHeader soapHeader) {

        try {
            X509Certificate recipientCertificate = new SmpLookupManager().getEndpointCertificate(
                    soapHeader.getRecipientIdentifier(),
                    soapHeader.getDocumentIdentifier());

            if (new KeystoreManager().isOurCertificate(recipientCertificate)) {
                Log.info("SMP lookup OK");
            } else {
                Log.info("SMP lookup indicates that document was sent to the wrong access point");
                throw new FaultMessage("This message was sent to the wrong Access Point", new StartException());
            }
        } catch (Exception e) {
            Log.info("SMP lookup fails, we assume the message is for us");
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
