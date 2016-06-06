/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.smp;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.security.CommonName;
import eu.peppol.security.SmpResponseValidator;
import eu.peppol.util.*;
import org.busdox.servicemetadata.publishing._1.EndpointType;
import org.busdox.servicemetadata.publishing._1.SignedServiceMetadataType;
import org.busdox.transport.identifiers._1.ProcessIdentifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread safe implementation of {@link SmpLookupManager}
 * <p/>
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:01:53 AM
 *
 * @author Nigel Parker
 * @author Steinar O. Cook
 * @author Thore Johnsen
 */
public class SmpLookupManagerImpl implements SmpLookupManager {

    private static final Logger log = LoggerFactory.getLogger(SmpLookupManagerImpl.class);
    private final OperationalMode operationalMode;
    private final SmlHost configuredSmlHost;

    private JAXBContext jaxbContext;

    private final DNSLookupHelper dnsLookupHelper;
    private final SmlHost smlHost;

    // Keeping the SMP content retriever in a separate class allows for unit testing
    private final SmpContentRetriever smpContentRetriever;
    private final BusDoxProtocolSelectionStrategy busDoxProtocolSelectionStrategy;

    @Inject
    public SmpLookupManagerImpl(SmpContentRetriever smpContentRetriever, BusDoxProtocolSelectionStrategy busDoxProtocolSelectionStrategy, OperationalMode operationalMode,  SmlHost configuredSmlHost) {
        this.operationalMode = operationalMode;
        this.configuredSmlHost = configuredSmlHost;

        this.smlHost = discoverSmlHost();
        this.smpContentRetriever = smpContentRetriever;
        this.busDoxProtocolSelectionStrategy = busDoxProtocolSelectionStrategy;
        this.dnsLookupHelper = new DNSLookupHelper();
        try {
            jaxbContext = JaxbContextCache.getInstance(SignedServiceMetadataType.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to create Jaxb context cache: " + e, e);
        }
    }

    /**
     * Discovers which SML host should be used.
     *
     * @return the SML host instance to be used
     */
     SmlHost discoverSmlHost() {
        SmlHost computedSmlHostName;
        switch (operationalMode) {
            case TEST:
                log.warn("Mode of operation is TEST");
                computedSmlHostName = SmlHost.TEST_SML;
                break;
            default:
                computedSmlHostName = SmlHost.PRODUCTION_SML;
                break;
        }

        // Finally we check to see if the SML hostname has been overridden in the configuration file
        computedSmlHostName = checkForSmlHostnameOverride(computedSmlHostName);

        log.debug("SML hostname: " + computedSmlHostName);
        return computedSmlHostName;
    }

    SmlHost checkForSmlHostnameOverride(SmlHost computedSmlHost) {
        SmlHost result;
        if (configuredSmlHost != null) {
            log.debug("SML hostname has been overridden: [" + configuredSmlHost + "]");
            result = configuredSmlHost;
        } else {
            result = computedSmlHost;
        }

        return result;
    }

    /**
     * Produces the endpoint URL for the supplied participant and document type identifier.
     *
     * @param participant identifies the participant for which we are performing a lookup
     * @param documentTypeIdentifier the document type identifier, which constitutes the second half of the lookup key.
     * @return The endpoint address for the participant and DocumentId
     * @throws RuntimeException If the end point address cannot be resolved for the participant. This is caused by a {@link java.net.UnknownHostException}
     */
    @Override
    public URL getEndpointAddress(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
        EndpointType endpointType = getEndpointType(participant, documentTypeIdentifier);
        String address = getEndPointUrl(endpointType);
        log.info("Found endpoint address for " + participant.stringValue() + " from SMP: " + address);
        try {
            return new URL(address);
        } catch (Exception e) {
            throw new RuntimeException("SMP returned invalid URL", e);
        }
    }

    /**
     * Provides the end point data required for transmission of a message.
     */
    @Override
    public PeppolEndpointData getEndpointTransmissionData(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) {
        EndpointType endpointType = getEndpointType(participantId, documentTypeIdentifier);
        String transportProfile = endpointType.getTransportProfile();
        String address = getEndPointUrl(endpointType);
        X509Certificate x509Certificate = getX509CertificateFromEndpointType(endpointType);
        try {
            return new PeppolEndpointData(new URL(address), BusDoxProtocol.instanceFrom(transportProfile), CommonName.valueOf(x509Certificate.getSubjectX500Principal()));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to provide end point data for " + participantId + " for " + documentTypeIdentifier.toString());
        }
    }

    /**
     * Retrieves the end point certificate for the given combination of receiving participant id and document type identifier.
     *
     * @param participant            receiving participant
     * @param documentTypeIdentifier document type to be sent
     * @return The X509Certificate for the given ParticipantId and DocumentId
     * @throws RuntimeException If the end point address cannot be resolved for the participant. This is caused by a {@link java.net.UnknownHostException}
     */
    @Override
    public X509Certificate getEndpointCertificate(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
        EndpointType endpointType = getEndpointType(participant, documentTypeIdentifier);
        return getX509CertificateFromEndpointType(endpointType);
    }

    /**
     * Retrieves a group of URLs representing the documents accepted by the given participant id
     *
     * @param participantId participant id to look up
     * @return list of URLs representing each document type accepted
     */
    @Override
    public List<PeppolDocumentTypeId> getServiceGroups(ParticipantId participantId) throws SmpLookupException, ParticipantNotRegisteredException {

        // Creates the URL for the service meta data for the supplied participant
        URL serviceGroupURL = constructServiceGroupURL(participantId);

        if (!isParticipantRegistered(serviceGroupURL)) {
            throw new ParticipantNotRegisteredException(participantId);
        }

        NodeList nodes;
        List<PeppolDocumentTypeId> result = new ArrayList<PeppolDocumentTypeId>();
        InputSource smpContents;

        /*
        When looking up ParticipantId("9908:976098897") we expected the SML not
        to resolve, but it actually did and we got a not found (HTTP 404) response
        from SMP instead (smp-basware.publisher.sml.peppolcentral.org).
        */
        try {
            smpContents = smpContentRetriever.getUrlContent(serviceGroupURL);
        } catch (ConnectionException ex) {
            if (404 == ex.getCode()) {
                // signal that we got a NOT FOUND for that participant in the SMP
                throw new ParticipantNotRegisteredException(participantId);
            } else {
                throw ex; // re-throw exception
            }
        }

        // Parses the XML response from the SMP
        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            // Prevents XML entity expansion attacks
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);

            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder;
            Document document;

            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(smpContents);

            // Locates the namespace URI of the root element
            String nameSpaceURI = document.getDocumentElement().getNamespaceURI();
            nodes = document.getElementsByTagNameNS(nameSpaceURI, "ServiceMetadataReference");

        } catch (Exception e) {
            throw new SmpLookupException(participantId, serviceGroupURL, e);
        }

        // Loop the SMR elements, if any, and populate the result list
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                String docTypeAsString = null;
                try {

                    // Fetch href attribute
                    Element element = (Element) nodes.item(i);
                    String hrefAsString = element.getAttribute("href");

                    // Gets rid of all the funny %3A's...
                    hrefAsString = URLDecoder.decode(hrefAsString, "UTF-8");

                    // Grabs the entire text string after "busdox-docid-qns::"
                    docTypeAsString = hrefAsString.substring(hrefAsString.indexOf("busdox-docid-qns::") + "busdox-docid-qns::".length());

                    // Parses and creates the document type id
                    PeppolDocumentTypeId peppolDocumentTypeId = PeppolDocumentTypeId.valueOf(docTypeAsString);

                    result.add(peppolDocumentTypeId);

                } catch (Exception e) {
                    /* ignore unparseable document types at runtime */
                    log.warn("Unable to create PeppolDocumentTypeId from " + docTypeAsString + ", got exception " + e.getMessage());
                }
            }
        }

        return result;

    }

    public PeppolProcessTypeId getProcessIdentifierForDocumentType(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) throws SmpSignedServiceMetaDataException {
        SignedServiceMetadataType serviceMetaData = getServiceMetaData(participantId, documentTypeIdentifier);
        // SOAP generated type...
        ProcessIdentifierType processIdentifier = serviceMetaData.getServiceMetadata().getServiceInformation().getProcessList().getProcess().get(0).getProcessIdentifier();
        // Converts SOAP generated type into something nicer
        return PeppolProcessTypeId.valueOf(processIdentifier.getValue());
    }

    /**
     * Retrieves the complete signed service meta data for a given participant and document type identifier.
     * @throws SmpSignedServiceMetaDataException
     */
    @Override
    public SignedServiceMetadataType getServiceMetaData(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) throws SmpSignedServiceMetaDataException {

        // Constructs the URL for looking up the participant and document type identifier
        URL smpUrl = constructDocumentTypeURL(participant, documentTypeIdentifier);

        // Retrieves the contents of the URL
        InputSource smpContents = fetchContentsOfSmpUrl(participant, documentTypeIdentifier, smpUrl);

        // Transforms the response received into an XML document
        Document document = null;
        try {
            document = createXmlDocument(smpContents);
        } catch (Exception e) {
            throw new SmpSignedServiceMetaDataException(participant, documentTypeIdentifier, smpUrl, e);
        }

        // Validates the signature
        SmpResponseValidator smpResponseValidator = new SmpResponseValidator(document);
        if (!smpResponseValidator.isSmpSignatureValid()) {
            throw new IllegalStateException("SMP response contained invalid signature");
        }

        // TODO Uncomment code below if PEPPOL decides we need to follow the chain of trust for the SMP certificate.
        // Validates the certificate supplied with the signature
        // if (!OxalisCertificateValidator.getInstance().validateUsingCache(smpResponseValidator.getCertificate())) {
        //     throw new IllegalStateException("SMP Certificate not valid for " + smpUrl);
        // }

        try {
            // Finally parse the response into a properly typed object
            return parseSmpResponseIntoSignedServiceMetadataType(document);
        } catch (Exception e) {
            throw new SmpSignedServiceMetaDataException(participant, documentTypeIdentifier, smpUrl, e);
        }
    }

    /**
     * Helper method, which extracts the URL of the end point.
     */
    private String getEndPointUrl(EndpointType endpointType) {
        return endpointType.getEndpointReference().getAddress().getValue();
    }

    /**
     * Constructs the URL used to look up all the service groups for a given participant identifier.
     * @param participantId participant for which we should perform the lookup
     * @return URL instance, which can be used to obtain service groups
     * @throws SmpLookupException
     */
    URL constructServiceGroupURL(ParticipantId participantId) throws SmpLookupException {
        String scheme = ParticipantId.getScheme();
        String value = participantId.stringValue();
        try {
            String hostname = "B-" + Util.calculateMD5(value.toLowerCase()) + "." + scheme + "." + smlHost;
            // Example: iso6523-actorid-upis%3A%3A9908:810017902
            String encodedParticipant = URLEncoder.encode(scheme + "::", "UTF-8") + value;
            return new URL("http://" + hostname + "/" + encodedParticipant);
        } catch (Exception e) {
            throw new SmpLookupException(participantId, e);
        }
    }

    /**
     * Constructs the URL used to obtain meta data for a given document type identifier of a given participant identifier.
     */
    URL constructDocumentTypeURL(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier)  {
        String scheme = ParticipantId.getScheme();
        String value = participantId.stringValue();
        String hostname = null;
        String urlString = null;

        try {
            hostname = "B-" + Util.calculateMD5(value.toLowerCase()) + "." + scheme + "." + smlHost;
            String encodedParticipant = URLEncoder.encode(scheme + "::" + value, "UTF-8");
            String encodedDocumentId = URLEncoder.encode(PeppolDocumentTypeIdAcronym.getScheme() + "::" + documentTypeIdentifier.toString(), "UTF-8");
            urlString = "http://" + hostname + "/" + encodedParticipant + "/services/" + encodedDocumentId;
            return new URL(urlString);
        } catch (MessageDigestException e) {
            throw new IllegalStateException("Unable to calculate message digest for: " + participantId + ", doc.type:" + documentTypeIdentifier, e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to encode _" + scheme + "::" + value, e);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to create URL from string:" + urlString,e);
        }
    }

    SignedServiceMetadataType parseSmpResponseIntoSignedServiceMetadataType(Document document) throws ParserConfigurationException, SAXException, IOException, JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(document, SignedServiceMetadataType.class).getValue();
    }

     // Parses the XML response from the SMP
    Document createXmlDocument(InputSource smpContents) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        // Prevents XML entity expansion attacks
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(smpContents);
    }

    /**
     * Helper method, which extracts a valid X509 certificate for an end point.
     */
    private X509Certificate getX509CertificateFromEndpointType(EndpointType endpointType) {
        try {
            String body = endpointType.getCertificate();
            String endpointCertificate = "-----BEGIN CERTIFICATE-----\n" + body + "\n-----END CERTIFICATE-----";
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(endpointCertificate.getBytes()));
            cert.checkValidity();
            return cert;
        } catch (CertificateException e) {
            throw new RuntimeException("Failed to get valid certificate from Endpoint data", e);
        }
    }

    /**
     * Each participant has its own sub-domain in peppolcentral, therefore if one does not
     * exist it means participant is not registered.
     */
    private boolean isParticipantRegistered(URL serviceGroupURL) {
        return dnsLookupHelper.domainExists(serviceGroupURL);
    }

    private InputSource fetchContentsOfSmpUrl(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier, URL smpUrl) throws SmpSignedServiceMetaDataException {
        InputSource smpContents;
        try {
            log.debug("Constructed SMP url: " + smpUrl.toExternalForm());
            smpContents = smpContentRetriever.getUrlContent(smpUrl);
        } catch (Exception e) {
            throw new SmpSignedServiceMetaDataException(participant, documentTypeIdentifier, smpUrl, e);
        }
        return smpContents;
    }

    /**
     * Retrieves an instance of the complete EndpointType for a given participant and the given document type identifier.
     * Given by the following XPath:
     * <pre>
     *     //ServiceMetadata/ServiceInformation/ProcessList/Process[0]/ServiceEndpointList/Endpoint[0]
     * </pre>
     *
     * @param participant the participant identifier
     * @param documentTypeIdentifier the document type identifier
     * @return the JAXB generated EndpointType object
     */
    private EndpointType getEndpointType(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {
        try {
            SignedServiceMetadataType serviceMetadata = getServiceMetaData(participant, documentTypeIdentifier);
            return selectOptimalEndpoint(serviceMetadata);
        } catch (Exception e) {
            String pid = (participant == null) ? "participant missing" : "for participant " + participant.toString();
            String did = (documentTypeIdentifier == null) ? "document type missing" : "document type " + documentTypeIdentifier.toString();
            throw new RuntimeException("Problem with SMP lookup " + pid + " and " + did, e);
        }
    }

    EndpointType selectOptimalEndpoint(SignedServiceMetadataType serviceMetadata) {

        // List of end points contained in the signed service meta data type
        List<EndpointType> endPointsForDocumentTypeIdentifier = serviceMetadata
                .getServiceMetadata()
                .getServiceInformation()
                .getProcessList()
                .getProcess()
                .get(0)
                .getServiceEndpointList()
                .getEndpoint();

        Map<BusDoxProtocol, EndpointType> protocolsAndEndpointType = new HashMap<BusDoxProtocol, EndpointType>();

        for (EndpointType endpointType : endPointsForDocumentTypeIdentifier) {
            try {
                BusDoxProtocol busDoxProtocol = BusDoxProtocol.instanceFrom(endpointType.getTransportProfile());
                protocolsAndEndpointType.put(busDoxProtocol, endpointType);
            } catch(Exception ex) {
                log.warn("Skipping endpoint, unable to handle protocol {}", endpointType.getTransportProfile());
            }
        }

        BusDoxProtocol preferredProtocol = busDoxProtocolSelectionStrategy.selectOptimalProtocol(new ArrayList<BusDoxProtocol>(protocolsAndEndpointType.keySet()));

        return protocolsAndEndpointType.get(preferredProtocol);

    }

}
