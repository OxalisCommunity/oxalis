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

package eu.peppol.smp;

import com.google.inject.Inject;
import eu.peppol.BusDoxProtocol;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeId;
import eu.peppol.security.CommonName;
import eu.peppol.security.KeystoreManager;
import eu.peppol.security.SmpResponseValidator;
import eu.peppol.start.identifier.*;
import eu.peppol.util.DNSLookupHelper;
import eu.peppol.util.GlobalConfiguration;
import eu.peppol.util.JaxbContextCache;
import eu.peppol.util.Util;
import org.busdox.smp.EndpointType;
import org.busdox.smp.ProcessIdentifierType;
import org.busdox.smp.SignedServiceMetadataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread safe implementation of {@link SmpLookupManager}
 * <p/>
 * User: nigel
 * Date: Oct 25, 2011
 * Time: 9:01:53 AM
 *
 * @author Nigel Parker
 * @author Steinar O. Cook
 */
public class SmpLookupManagerImpl implements SmpLookupManager {

    public static final Logger log = LoggerFactory.getLogger(SmpLookupManagerImpl.class);

    private JAXBContext jaxbContext;

    private KeystoreManager keystoreManager;
    private DNSLookupHelper dnsLookupHelper;
    private SmlHost smlHost;

    public SmpLookupManagerImpl() {
        this(discoverSmlHost());
    }

    static SmlHost discoverSmlHost() {
        SmlHost smlHost = null;
        switch (GlobalConfiguration.getInstance().getModeOfOperation()) {
            case TEST:
                log.warn("Mode of operation is TEST");
                smlHost = SmlHost.TEST_SML;
                break;
            default:
                smlHost = SmlHost.PRODUCTION_SML;
                break;
        }

        smlHost = checkForSmlHostnameOverride(smlHost);

        log.debug("SML hostname: " + smlHost);
        return smlHost;
    }

    static SmlHost checkForSmlHostnameOverride(SmlHost smlHost) {
        String smlHostname = GlobalConfiguration.getInstance().getSmlHostname();
        if (!String.valueOf(smlHostname).isEmpty()) {
            log.debug("SML hostname has been overridden: [" + smlHostname + "]");
            smlHost = SmlHost.valueOf(smlHostname);
        }
        return smlHost;
    }

    public SmpLookupManagerImpl(SmlHost smlHost) {
        this.smlHost = smlHost;
        this.keystoreManager = KeystoreManager.getInstance();
        this.dnsLookupHelper = new DNSLookupHelper();
        try {
            jaxbContext = JaxbContextCache.getInstance(SignedServiceMetadataType.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to create Jaxb context cache: " + e, e);
        }
    }

    /**
     * @param participant
     * @param documentTypeIdentifier
     * @return The endpoint address for the participant and DocumentId
     * @throws RuntimeException If the end point address cannot be resolved for the participant. This is caused by a {@link java.net.UnknownHostException}
     */
    @Override
    public URL getEndpointAddress(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {

        EndpointType endpointType = getEndpointType(participant, documentTypeIdentifier);
        String address = getEndPointUrl(endpointType);
        Log.info("Found endpoint address for " + participant.stringValue() + " from SMP: " + address);

        try {
            return new URL(address);
        } catch (Exception e) {
            throw new RuntimeException("SMP returned invalid URL", e);
        }
    }

    @Override
    public PeppolEndpointData getEndpointData(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) {
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

    private String getEndPointUrl(EndpointType endpointType) {
        return endpointType.getEndpointReference().getAddress().getValue();
    }

    /**
     * Retrieves the end point certificate for the given combination of receiving participant id and document type identifer.
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

    private X509Certificate getX509CertificateFromEndpointType(EndpointType endpointType) {
        try {
            String body = endpointType.getCertificate();
            String endpointCertificate = "-----BEGIN CERTIFICATE-----\n" + body + "\n-----END CERTIFICATE-----";
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(endpointCertificate.getBytes()));

        } catch (CertificateException e) {
            throw new RuntimeException("Failed to get certificate from Endpoint data");
        }
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
        URL serviceGroupURL = getServiceGroupURL(participantId);

        if (!isParticipantRegistered(serviceGroupURL)) {
            throw new ParticipantNotRegisteredException(participantId);
        }

        NodeList nodes;
        List<PeppolDocumentTypeId> result = new ArrayList<PeppolDocumentTypeId>();
        InputSource smpContents = Util.getUrlContent(serviceGroupURL);

        // Parses the XML response from the SMP
        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = null;
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
                    Log.warn("Unable to create PeppolDocumentTypeId from " + docTypeAsString + ", got exception " + e.getMessage());
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


    URL getServiceGroupURL(ParticipantId participantId) throws SmpLookupException {
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
     * Each participant has its own sub-domain in peppolcentral, therefore if one does not
     * exist it means participant is not registered.
     */
    private boolean isParticipantRegistered(URL serviceGroupURL) {
        return dnsLookupHelper.domainExists(serviceGroupURL);
    }

    private URL getSmpUrl(ParticipantId participantId, PeppolDocumentTypeId documentTypeIdentifier) throws Exception {

        String scheme = ParticipantId.getScheme();
        String value = participantId.stringValue();
        String hostname = "B-" + Util.calculateMD5(value.toLowerCase()) + "." + scheme + "." + smlHost;
        String encodedParticipant = URLEncoder.encode(scheme + "::" + value, "UTF-8");
        String encodedDocumentId = URLEncoder.encode(PeppolDocumentTypeIdAcronym.getScheme() + "::" + documentTypeIdentifier.toString(), "UTF-8");

        return new URL("http://" + hostname + "/" + encodedParticipant + "/services/" + encodedDocumentId);
    }

    public SignedServiceMetadataType getServiceMetaData(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) throws SmpSignedServiceMetaDataException {

        URL smpUrl = null;
        try {
            smpUrl = getSmpUrl(participant, documentTypeIdentifier);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to construct URL for " + participant + ", documentType" + documentTypeIdentifier + "; " + e.getMessage(), e);
        }

        InputSource smpContents = null;
        try {
            Log.debug("Constructed SMP url: " + smpUrl.toExternalForm());
            smpContents = Util.getUrlContent(smpUrl);
        } catch (Exception e) {
            throw new SmpSignedServiceMetaDataException(participant, documentTypeIdentifier, smpUrl, e);
        }

        try {

            // Parses the XML response from the SMP
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(smpContents);

            // Validates the signature
            SmpResponseValidator smpResponseValidator = new SmpResponseValidator(document);
            if (!smpResponseValidator.isSmpSignatureValid()) {
                throw new IllegalStateException("SMP response contained invalid signature");
            }

/**
 * Uncomment code below if PEPPOL decides we need to follow the chain of trust for the SMP certificate.
 */
            // Validates the certificate supplied with the signature
/*
            if (!OxalisCertificateValidator.getInstanceForRawStatistics().validateUsingCache(smpResponseValidator.getCertificate())) {
                throw new IllegalStateException("SMP Certificate not valid for " + smpUrl);
            }
*/

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            return unmarshaller.unmarshal(document, SignedServiceMetadataType.class).getValue();
        } catch (Exception e) {
            throw new SmpSignedServiceMetaDataException(participant, documentTypeIdentifier, smpUrl, e);
        }
    }

    /**
     * Retrieves an instance of the complete EndpointType for a given participant and the given document type identifier.
     * Given by the following XPath:
     * <pre>
     *     //ServiceMetadata/ServiceInformation/ProcessList/Process[0]/ServiceEndpointList/Endpoint[0]
     * </pre>
     *
     * @param participant
     * @param documentTypeIdentifier
     * @return
     */
    private EndpointType getEndpointType(ParticipantId participant, PeppolDocumentTypeId documentTypeIdentifier) {

        try {
            SignedServiceMetadataType serviceMetadata = getServiceMetaData(participant, documentTypeIdentifier);

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
}
