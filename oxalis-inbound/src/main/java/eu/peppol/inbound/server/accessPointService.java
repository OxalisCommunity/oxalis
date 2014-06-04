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

import com.google.inject.Inject;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.XWSSecurityException;
import eu.peppol.BusDoxProtocol;
import eu.peppol.PeppolMessageMetaData;
import eu.peppol.identifier.AccessPointIdentifier;
import eu.peppol.inbound.guice.GuiceManaged;
import eu.peppol.inbound.guice.RepositoryModule;
import eu.peppol.inbound.soap.PeppolMessageHeaderParser;
import eu.peppol.inbound.util.Log;
import eu.peppol.persistence.MessageRepository;
import eu.peppol.persistence.OxalisMessagePersistenceException;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.smp.SmpLookupManager;
import eu.peppol.smp.SmpLookupManagerImpl;
import eu.peppol.smp.SmpModule;
import eu.peppol.start.identifier.ChannelId;
import eu.peppol.statistics.*;
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
import javax.security.auth.x500.X500Principal;
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
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings({"UnusedDeclaration"})
// Configures the access point service using Guice
@GuiceManaged(modules = {RepositoryModule.class, SmpModule.class})

@WebService(serviceName = "accessPointService", portName = "ResourceBindingPort", endpointInterface = "org.w3._2009._02.ws_tra.Resource", targetNamespace = "http://www.w3.org/2009/02/ws-tra", wsdlLocation = "WEB-INF/wsdl/accessPointService/wsdl_v2.0.wsdl")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
@HandlerChain(file = "handler-chain.xml")
@Addressing
public class accessPointService {

    public static final Logger log = LoggerFactory.getLogger(accessPointService.class);

    private final RawStatisticsRepositoryFactory rawStatisticsRepositoryFactory;
    private final GlobalConfiguration globalConfiguration;
    private final AccessPointIdentifier ourAccessPointIdentifier;


    public accessPointService() {
        log.info("Attempting to create the AccessPointService ...");

        rawStatisticsRepositoryFactory = RawStatisticsRepositoryFactoryProvider.getInstance();
        globalConfiguration = GlobalConfiguration.getInstance();

        ourAccessPointIdentifier = AccessPointIdentifier.valueOf(KeystoreManager.getInstance().getOurCommonName());

        log.info("AccessPointService created ...");
    }

    // When using Guice, the @Resource annotation does not work, must use @Inject, probably due to some proxy problems
    // @javax.annotation.Resource
    @Inject
    WebServiceContext webServiceContext;

    @Inject
    PeppolMessageHeaderParser peppolMessageHeaderParser;

    @Inject
    MessageRepository messageRepository;

    @Inject
    RawStatisticsRepository rawStatisticsRepository;

    @Inject
    SmpLookupManager smpLookupManager;

    @Action(input = "http://www.w3.org/2009/02/ws-tra/Create",
            output = "http://www.w3.org/2009/02/ws-tra/CreateResponse",
            fault = {@FaultAction(className = org.w3._2009._02.ws_tra.FaultMessage.class,
                    value = "http://busdox.org/2010/02/channel/fault")})
    public CreateResponse create(Create body) throws FaultMessage, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, IOException, KeyStoreException {

        CreateResponse createResponse = null;
        try {

            if (webServiceContext == null) {
                throw new IllegalStateException("WebServiceContext not injected!");
            }

            // Retrieves the PEPPOL message header
            PeppolMessageMetaData peppolMessageMetaData = getPeppolMessageMetaData();


            log.info("Received message " + peppolMessageMetaData);

            // Injects current context into SLF4J Mapped Diagnostic Context
            setUpSlf4JMDC(peppolMessageMetaData);

            // This does not seem to give any meaning, unless the spec says so, this code is obsolete.
            verifyThatThisDocumentIsForUs(peppolMessageMetaData);

            Document document = ((Element) body.getAny().get(0)).getOwnerDocument();

            // Invokes the message persistence
            persistMessage(peppolMessageMetaData, document);

            createResponse = new CreateResponse();

            // Persists the statistical information
            persistStatistics(peppolMessageMetaData);

            displayMemoryUsage();

            // Clears the SLF4J Message Diagnostic Context
            MDC.clear();


        } catch (OxalisStatisticsPersistenceException e) {
            log.error("Persistence of statistics failed: " + e.getMessage(), e);
            log.error("Message has been persisted and confirmation sent, but you must investigate this error");
        } catch (OxalisMessagePersistenceException e) {
            log.error("Unable to persist received message: " + e.getMessage(), e);
            log.error("Throwing FaultException back to client");
            FaultMessage serverException = FaultExceptionFactory.createServerException("Unable to persist received message",e);
            throw serverException;

        } catch (Exception e) {

            // Wraps the message in a FaultMessage(StartException)
            FaultMessage faultMessage = FaultExceptionFactory.createServerException(e.getMessage(), e);
            log.error("Problem while handling inbound document: " + e.getMessage(), e);
            log.error("Throwing FaultException back to client");

            throw faultMessage;
        } finally {
            MDC.clear();
        }
        return createResponse;
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
     * @param document the XML document.
     */
    void persistMessage(PeppolMessageMetaData peppolMessageMetaData, Document document) throws OxalisMessagePersistenceException {
        messageRepository.saveInboundMessage(peppolMessageMetaData, document);
    }

    private PeppolMessageMetaData getPeppolMessageMetaData() {
        MessageContext messageContext = webServiceContext.getMessageContext();
        HeaderList headerList = (HeaderList) messageContext.get(JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);

        // Parses the headers in the SOAP message into the meta data
        PeppolMessageMetaData peppolMessageMetaData = peppolMessageHeaderParser.parseSoapHeaders(headerList);

        // The Principal of the remote host is just a difficult word for their X.509 certificate Distinguished Name (DN)
        Principal remoteAccessPointPrincipal = fetchAccessPointPrincipal(webServiceContext);
        peppolMessageMetaData.setSendingAccessPointPrincipal(remoteAccessPointPrincipal);

        // If the Principal contains CN= etc., saves it into meta data
        if (remoteAccessPointPrincipal instanceof X500Principal) {
            CommonName remoteAccessPointCommonName = CommonName.valueOf((X500Principal) remoteAccessPointPrincipal);
            peppolMessageMetaData.setSendingAccessPoint( AccessPointIdentifier.valueOf(remoteAccessPointCommonName));
        }

        peppolMessageMetaData.setReceivingAccessPoint(ourAccessPointIdentifier);
        peppolMessageMetaData.setProtocol(BusDoxProtocol.START);

        return peppolMessageMetaData;
    }

    void setUpSlf4JMDC(PeppolMessageMetaData peppolMessageMetaData) {
        MDC.put("transmissionId", peppolMessageMetaData.getTransmissionId().toString());
        MDC.put("senderId", peppolMessageMetaData.getSenderId().toString());
    }

    /**
     * Inspects the data in the message header to determine whether our access point is the correct destination
     * for the message or not.
     * <p/>
     * This is done by comparing the certificate of the destination end point found in the SMP with our
     * certificate. If they are the same, we are obviously the correct receiver of the message.
     *
     * @param peppolMessageMetaData
     */
    void verifyThatThisDocumentIsForUs(PeppolMessageMetaData peppolMessageMetaData) {

        try {
            X509Certificate recipientCertificate = smpLookupManager.getEndpointCertificate(
                    peppolMessageMetaData.getRecipientId(),
                    peppolMessageMetaData.getDocumentTypeIdentifier());

            if (KeystoreManager.getInstance().isOurCertificate(recipientCertificate)) {
                Log.info("SMP certificate for receiver matches our AP certificate - OK");
            } else {
                Log.error("SMP certificate for receiver does NOT match our access point certificate.");
                throw new FaultMessage("SMP certificate for receiver does NOT match our access point certificate.", new StartException());
            }
        } catch (Exception e) {
            Log.info("Error ignored, we assume the message is for us anyway");
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
    public static String displayMemoryUsage() {

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

    /**
     * Persists statistics, if an error occurs, logs an error, but does not throw exception in order to ensure that
     * operations are affected by problems with statistics.
     *
     * @param peppolMessageMetaData
     */
    void persistStatistics(PeppolMessageMetaData peppolMessageMetaData) throws OxalisStatisticsPersistenceException {
        try {
            RawStatistics rawStatistics = new RawStatistics.RawStatisticsBuilder()
                    .accessPointIdentifier(ourAccessPointIdentifier)
                    .inbound()
                    .documentType(peppolMessageMetaData.getDocumentTypeIdentifier())
                    .sender(peppolMessageMetaData.getSenderId())
                    .receiver(peppolMessageMetaData.getRecipientId())
                    .profile(peppolMessageMetaData.getProfileTypeIdentifier())
                    .channel(new ChannelId("START"))
                    .build();
            rawStatisticsRepository.persist(rawStatistics);
        } catch (Exception e) {
            log.error("Unable to persist statistics for " + peppolMessageMetaData.toString() + "; " + e.getMessage(), e);
            throw new OxalisStatisticsPersistenceException(peppolMessageMetaData, e);
        }
    }

}
