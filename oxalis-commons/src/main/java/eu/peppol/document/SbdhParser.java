package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

/**
 * Parses the SBDH into a PeppolMessageInformation instance
 *
 * @author steinar
 *         Date: 25.10.13
 *         Time: 09:45
 */
public class SbdhParser {


    public static final Logger log = LoggerFactory.getLogger(SbdhParser.class);
    private final JAXBContext jaxbContext;
    private final XMLInputFactory xmlInputFactory;

    public SbdhParser() {
        try {
            jaxbContext = JAXBContext.newInstance("org.unece.cefact.namespaces.standardbusinessdocumentheader");
        } catch (JAXBException e) {
            String msg = "Unable to initialize the JAXBContext: " + e.getMessage();
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }

        xmlInputFactory = XMLInputFactory.newInstance();


    }

    /** Parses the SBDH from the provided stream into a PeppolMessageInformation object which is created here */
    public PeppolStandardBusinessHeader parse(InputStream inputStream) {


        Unmarshaller unmarshaller = createUnmarshaller();

        XMLStreamReader xmlReader = createXmlStreamRader(inputStream);

        PeppolStandardBusinessHeader peppolSbdh = new PeppolStandardBusinessHeader();
        try {

            JAXBElement<StandardBusinessDocument> sbdh = (JAXBElement) unmarshaller.unmarshal(xmlReader);

            StandardBusinessDocument standardBusinessDocument = sbdh.getValue();
            StandardBusinessDocumentHeader standardBusinessDocumentHeader = standardBusinessDocument.getStandardBusinessDocumentHeader();


            // Receiver
            String receiver = getReceiver(standardBusinessDocumentHeader);
            peppolSbdh.setRecipientId(new ParticipantId(receiver));

            // Sender
            String sender = getSender(standardBusinessDocumentHeader);
            peppolSbdh.setSenderId(new ParticipantId(sender));

            // Message id
            String messageId = getMessageId(standardBusinessDocumentHeader);
            peppolSbdh.setMessageId(new MessageId(messageId));

            // Computes the document type and process/profile type identifier
            parseDocumentIdentificationAndScopes(peppolSbdh,standardBusinessDocumentHeader);

            XMLGregorianCalendar creationDateAndTime = standardBusinessDocumentHeader.getDocumentIdentification().getCreationDateAndTime();
            Calendar cal = creationDateAndTime.toGregorianCalendar();

            peppolSbdh.setCreationDateAndTime(cal.getTime());

            return peppolSbdh;
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to parse SBDH: " + e.getMessage(), e);
        }
    }

    private void parseDocumentIdentificationAndScopes(PeppolStandardBusinessHeader peppolMessageMetaData, StandardBusinessDocumentHeader standardBusinessDocumentHeader) {

        DocumentIdentification documentIdentification = standardBusinessDocumentHeader.getDocumentIdentification();

        List<Scope> scopes = standardBusinessDocumentHeader.getBusinessScope().getScope();

        for (Scope scope : scopes) {
            if (scope.getType().equalsIgnoreCase("DOCUMENTID")) {
                String rootNameSpace = documentIdentification.getStandard();
                String localRootName = documentIdentification.getType();
                String version = documentIdentification.getTypeVersion();
                String customization = scope.getInstanceIdentifier();

                PeppolDocumentTypeId peppolDocumentTypeId = new PeppolDocumentTypeId(rootNameSpace, localRootName, CustomizationIdentifier.valueOf(customization), version);
                peppolMessageMetaData.setDocumentTypeIdentifier(peppolDocumentTypeId);
                continue;
            }

            if (scope.getType().equalsIgnoreCase("PROCESSID")) {
                String processTypeIdentifer = scope.getInstanceIdentifier();
                peppolMessageMetaData.setProfileTypeIdentifier(new PeppolProcessTypeId(processTypeIdentifer));
                continue;
            }
        }
    }

    private String getSender(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        Partner partner = standardBusinessDocumentHeader.getSender().get(0);
        return partner.getIdentifier().getValue();
    }

    private String getMessageId(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        DocumentIdentification documentIdentification = standardBusinessDocumentHeader.getDocumentIdentification();
        String instanceIdentifier = documentIdentification.getInstanceIdentifier();
        return instanceIdentifier;
    }


    private String getReceiver(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        Partner partner = standardBusinessDocumentHeader.getReceiver().get(0);
        PartnerIdentification identifier = partner.getIdentifier();
        return identifier.getValue();
    }

    Unmarshaller createUnmarshaller() {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return  unmarshaller;
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to create JAXB unmarshaller: " + e.getMessage(), e);
        }
    }

    XMLEventReader createXmlEventReader(InputStream inputStream) {
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream);
            return xmlEventReader;
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to create XML reader: " + e.getMessage(), e);
        }
    }

    XMLStreamReader createXmlStreamRader(InputStream inputStream) {
        try {
            return xmlInputFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to crate XML Stream Reader: " + e.getMessage(), e);
        }
    }

}

