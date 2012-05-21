package eu.peppol.inbound.server;

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.developer.JAXWSProperties;
import eu.peppol.inbound.soap.PeppolMessageHeaderParser;
import eu.peppol.inbound.util.Log;
import eu.peppol.outbound.smp.SmpLookupManager;
import eu.peppol.start.identifier.Configuration;
import eu.peppol.start.identifier.KeystoreManager;
import eu.peppol.start.identifier.PeppolMessageHeader;
import eu.peppol.start.persistence.MessageRepository;
import eu.peppol.start.persistence.MessageRepositoryFactory;
import org.slf4j.MDC;
import org.w3._2009._02.ws_tra.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.jws.WebService;
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

@SuppressWarnings({"UnusedDeclaration"})
@WebService(serviceName = "accessPointService", portName = "ResourceBindingPort", endpointInterface = "org.w3._2009._02.ws_tra.Resource", targetNamespace = "http://www.w3.org/2009/02/ws-tra", wsdlLocation = "WEB-INF/wsdl/accessPointService/wsdl_v2.0.wsdl")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
@Addressing
public class accessPointService {

    @javax.annotation.Resource
    private WebServiceContext webServiceContext;

    @Action(input = "http://www.w3.org/2009/02/ws-tra/Create",
            output = "http://www.w3.org/2009/02/ws-tra/CreateResponse",
            fault = {@FaultAction(className = org.w3._2009._02.ws_tra.FaultMessage.class,
                    value = "http://busdox.org/2010/02/channel/fault")})
    public CreateResponse create(Create body) throws FaultMessage, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, IOException, KeyStoreException {

        try {

            PeppolMessageHeader messageHeader = getPeppolMessageHeader();
            Log.info("Received PEPPOL SOAP Header:" + messageHeader);

            // TODO: Verifies the SOAP header and rejects illegal messages

            // Injects current context into SLF4J Mapped Diagnostic Context
            setUpSlf4JMDC(messageHeader);
            verifyThatThisDocumentIsForUs(messageHeader);
            Document document = ((Element) body.getAny().get(0)).getOwnerDocument();

            // Invokes the message persistence
            persistMessage(messageHeader, document);

            CreateResponse createResponse = new CreateResponse();

            getMemoryUsage();
            return createResponse;

        } catch (Exception e) {
            Log.error("Problem while handling inbound document: " + e.getMessage(), e);
            throw new FaultMessage("Unexpected error in document handling: " + e.getMessage(), new StartException(),e);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Extracts metadata from the SOAP Header, i.e. the routing information and invokes a pluggable
     * message persistence in order to allow for storage of the meta data and the message itself.
     *
     * @param document the XML document.
     */
    void persistMessage(PeppolMessageHeader messageHeader, Document document) {

        // Invokes whatever has been configured in META-INF/services/.....
        try {

            String inboundMessageStore = Configuration.getInstance().getInboundMessageStore();
            // Locates a message repository using the META-INF/services mechanism
            MessageRepository messageRepository = MessageRepositoryFactory.getInstance();
            // Persists the message
            messageRepository.saveInboundMessage(inboundMessageStore, messageHeader, document);

        } catch (Throwable e) {
            Log.error("Unable to persist", e);
        }
    }

    private PeppolMessageHeader getPeppolMessageHeader() {
        MessageContext messageContext = webServiceContext.getMessageContext();
        HeaderList headerList = (HeaderList) messageContext.get(JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);
        PeppolMessageHeader peppolMessageHeader = PeppolMessageHeaderParser.parseSoapHeaders(headerList);
        return peppolMessageHeader;
    }

    void setUpSlf4JMDC(PeppolMessageHeader messageHeader) {
        MDC.put("msgId", messageHeader.getMessageId().toString());
        MDC.put("senderId", messageHeader.getSenderId().toString());
        MDC.put("channelId", messageHeader.getChannelId().toString());
    }

    private void verifyThatThisDocumentIsForUs(PeppolMessageHeader messageHeader) {

        try {
            X509Certificate recipientCertificate = new SmpLookupManager().getEndpointCertificate(
                    messageHeader.getRecipientId(),
                    messageHeader.getDocumentTypeIdentifier());

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

    private static final long MEMORY_THRESHOLD = 10;
    private static long lastUsage = 0;

    /**
     * returns a String describing current memory utilization. In addition unusually large
     * changes in memory usage will be logged.
     */
    public static String getMemoryUsage() {

        System.gc();
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - freeMemory;
        final long mega = 1048576;
        long usedInMegabytes = usedMemory / mega;
        long totalInMegabytes = totalMemory / mega;
        String memoryStatus = usedInMegabytes + "M / " + totalInMegabytes + "M / " + (runtime.maxMemory() / mega) + "M";

        if (usedInMegabytes <= lastUsage - MEMORY_THRESHOLD || usedInMegabytes >= lastUsage + MEMORY_THRESHOLD) {
            String threadName = Thread.currentThread().getName();
            System.out.println("%%% [" + threadName + "] Memory usage: " + memoryStatus);
            lastUsage = usedInMegabytes;
        }

        return memoryStatus;
    }
}
