/*
 * Copyright (c) 2011,2012,2013 UNIT4 Agresso AS.
 *
 * This file is part of Oxalis.
 *
 * Oxalis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Oxalis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Oxalis.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.peppol.inbound.server;

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.XWSSecurityException;
import eu.peppol.inbound.soap.PeppolMessageHeaderParser;
import eu.peppol.inbound.util.Log;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.start.identifier.AccessPointIdentifier;
import eu.peppol.start.identifier.KeystoreManager;
import eu.peppol.start.identifier.PeppolMessageHeader;
import eu.peppol.start.persistence.MessageRepository;
import eu.peppol.start.persistence.MessageRepositoryFactory;
import eu.peppol.statistics.RawStatistics;
import eu.peppol.statistics.StatisticsRepository;
import eu.peppol.statistics.StatisticsRepositoryFactory;
import eu.peppol.statistics.StatisticsRepositoryFactoryProvider;
import eu.peppol.util.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.w3._2009._02.ws_tra.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.Action;
import javax.xml.ws.BindingType;
import javax.xml.ws.FaultAction;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings({"UnusedDeclaration"})
@WebService(serviceName = "accessPointService", portName = "ResourceBindingPort", endpointInterface = "org.w3._2009._02.ws_tra.Resource", targetNamespace = "http://www.w3.org/2009/02/ws-tra", wsdlLocation = "WEB-INF/wsdl/accessPointService/wsdl_v2.0.wsdl")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
@HandlerChain(file = "handler-chain.xml")
@Addressing
public class accessPointService {

    public static final Logger log = LoggerFactory.getLogger(accessPointService.class);

    private final StatisticsRepositoryFactory statisticsRepositoryFactory;
    private final GlobalConfiguration globalConfiguration;
    private final AccessPointIdentifier accessPointIdentifier;

    public accessPointService() {
        System.out.println("Attempting to create the AccessPointService ...");

        statisticsRepositoryFactory = StatisticsRepositoryFactoryProvider.getInstance();
        globalConfiguration = GlobalConfiguration.getInstance();
        accessPointIdentifier = globalConfiguration.getAccessPointIdentifier();
        System.out.println("AccessPointService created ...");
    }

    @javax.annotation.Resource
    private WebServiceContext webServiceContext;

    @Action(input = "http://www.w3.org/2009/02/ws-tra/Create",
            output = "http://www.w3.org/2009/02/ws-tra/CreateResponse",
            fault = {@FaultAction(className = org.w3._2009._02.ws_tra.FaultMessage.class,
                    value = "http://busdox.org/2010/02/channel/fault")})
    public CreateResponse create(Create body) throws FaultMessage, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, IOException, KeyStoreException {

        try {


            // Retrieves the PEPPOL message header
            PeppolMessageHeader messageHeader = getPeppolMessageHeader();

            // For testing purposes, allows client to force a fault
            if (messageHeader.getChannelId().stringValue().contains("FAULT")) {
                throw new IllegalStateException("Keyword FAULT seen in channel");
            }

            log.info("Received message "  + messageHeader);

            // Injects current context into SLF4J Mapped Diagnostic Context
            setUpSlf4JMDC(messageHeader);

            // This does not seem to give any meaning, unless the spec says so, this code is obsolete.
            // verifyThatThisDocumentIsForUs(messageHeader);

            Document document = ((Element) body.getAny().get(0)).getOwnerDocument();
            // Invokes the message persistence
            persistMessage(messageHeader, document);

            CreateResponse createResponse = new CreateResponse();

            // Persists the statistical information
            persistStatistics(messageHeader);

            getMemoryUsage();

            // Clears the SLF4J Message Diagnostic Context
            MDC.clear();

            return createResponse;

        } catch (Exception e) {
            Log.error("Problem while handling inbound document: " + e.getMessage(), e);

            StartException faultInfo = new StartException();
            faultInfo.setAction("http://busdox.org/2010/02/channel/fault");
            faultInfo.setFaultcode("s:Sender");
            faultInfo.setFaultstring("ServerError");
            faultInfo.setDetails("Unexpected error in document handling: " + e.getMessage());

            throw new FaultMessage("ERROR:", faultInfo, e);
        } finally {
            MDC.clear();
        }
    }

    private Principal fetchAccessPointPrincipal(WebServiceContext webServiceContext) {

        // Retrieves the Principal from the request
        Subject subj = null;
        try {
            subj = getSubjectFromSoapRequest(webServiceContext);
        } catch (XWSSecurityException e) {
            log.warn("Unable to retrieve security Subject from SOAP request" + e.getMessage(), e);
            return new Principal() {

                @Override
                public String getName() {
                    return "AP Subject retrieval failed";
                }
            };
        }

        Set<Principal> principals = subj.getPrincipals();

        // A Subject may have several Principal objects associated, fetch the first
        Iterator<Principal> principalIterator = principals.iterator();
        if (principalIterator.hasNext()) {
            Principal principal = principalIterator.next();
            return principal;
        }

        return new Principal() {
            @Override
            public String getName() {
                return "unknown AP principal";
            }
        };
    }

    private Subject getSubjectFromSoapRequest(WebServiceContext webServiceContext) throws XWSSecurityException {
        return SubjectAccessor.getRequesterSubject(webServiceContext);
    }


    /**
     * Extracts metadata from the SOAP Header, i.e. the routing information and invokes a pluggable
     * message persistence in order to allow for storage of the meta data and the message itself.
     *
     * @param document                   the XML document.
     *
     */
    void persistMessage(PeppolMessageHeader messageHeader, Document document) {

        // Invokes whatever has been configured in META-INF/services/.....
        try {

            String inboundMessageStore = GlobalConfiguration.getInstance().getInboundMessageStore();
            // Locates a message repository using the META-INF/services mechanism
            MessageRepository messageRepository = MessageRepositoryFactory.getInstance();
            // Persists the message
            messageRepository.saveInboundMessage(inboundMessageStore, messageHeader, document);

        } catch (Throwable e) {
            Log.error("Unable to persist: " + e.getMessage(), e);

            throw new IllegalStateException("Unable to persist message " + messageHeader + "; " + e.getMessage(), e);
        }
    }

    private PeppolMessageHeader getPeppolMessageHeader() {
        MessageContext messageContext = webServiceContext.getMessageContext();
        HeaderList headerList = (HeaderList) messageContext.get(JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);
        PeppolMessageHeader peppolMessageHeader = PeppolMessageHeaderParser.parseSoapHeaders(headerList);

        // Retrieves the IP address or hostname of the remote host, which is useful for auditing.
        HttpServletRequest request = (HttpServletRequest) webServiceContext.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        peppolMessageHeader.setRemoteHost(request.getRemoteHost());

        // The Principal of the remote host is just a difficult word for their X.509 certificate Distinguished Name (DN)
        Principal remoteAccessPointPrincipal = fetchAccessPointPrincipal(webServiceContext);
        peppolMessageHeader.setRemoteAccessPointPrincipal(remoteAccessPointPrincipal);

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

            if (KeystoreManager.getInstance().isOurCertificate(recipientCertificate)) {
                Log.info("SMP lookup OK");
            } else {
                Log.info("SMP lookup indicates that document was sent to the wrong access point");
                throw new FaultMessage("This message was sent to the wrong Access Point", new StartException());
            }
        } catch (Exception e) {
            Log.info("SMP lookup fails, we assume the message is for us; reason " + e.getMessage());
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

    void persistStatistics(PeppolMessageHeader messageHeader) {
        RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder()
                .accessPointIdentifier(accessPointIdentifier)   // Identifier predefined in Oxalis global config file
                .inbound()
                .documentType(messageHeader.getDocumentTypeIdentifier())
                .sender(messageHeader.getSenderId())
                .receiver(messageHeader.getRecipientId())
                .profile(messageHeader.getPeppolProcessTypeId())
                .channel(messageHeader.getChannelId())
                .build();

        StatisticsRepository statisticsRepository = statisticsRepositoryFactory.getInstance();
        statisticsRepository.persist(rawStatistics);
    }

}
