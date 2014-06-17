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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Parses the SBDH into a PeppolStandardBusinessHeader instance
 *
 * @author steinar
 * @author thore
 *
 */
public class SbdhParser {

    public static final Logger log = LoggerFactory.getLogger(SbdhParser.class);

    private final JAXBContext jaxbContext;
    private final XMLInputFactory xmlInputFactory;

    public SbdhParser() {
        try {
            jaxbContext = JAXBContext.newInstance("org.unece.cefact.namespaces.standardbusinessdocumentheader");
            xmlInputFactory = XMLInputFactory.newInstance();
        } catch (Exception e) {
            String msg = "Unable to initialize the SbdhParser: " + e.getMessage();
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    public PeppolStandardBusinessHeader parse(InputStream inputStream) {

        Unmarshaller unmarshaller = createUnmarshaller();
        XMLStreamReader xmlReader = createXmlStreamReader(inputStream);

        try {

            PeppolStandardBusinessHeader peppolSbdh = new PeppolStandardBusinessHeader();

            // Let JAXB unmarshal the SBD/SBDH fragment (skipping payload, since it is commented out in the xsd)
            JAXBElement root = (JAXBElement) unmarshaller.unmarshal(xmlReader);
            StandardBusinessDocument standardBusinessDocument = (StandardBusinessDocument) root.getValue();
            StandardBusinessDocumentHeader standardBusinessDocumentHeader = standardBusinessDocument.getStandardBusinessDocumentHeader();

            // Skipping Header version and manifest (not used right now)

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
            parseDocumentIdentificationAndProfileIdentification(peppolSbdh, standardBusinessDocumentHeader);

            // Date / time conversion
            XMLGregorianCalendar creationDateAndTime = standardBusinessDocumentHeader.getDocumentIdentification().getCreationDateAndTime();
            Calendar cal = creationDateAndTime.toGregorianCalendar();
            peppolSbdh.setCreationDateAndTime(cal.getTime());

            return peppolSbdh;

        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to parse SBDH: " + e.getMessage(), e);
        }

    }

    private void parseDocumentIdentificationAndProfileIdentification(PeppolStandardBusinessHeader peppolMessageMetaData, StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        for (Scope scope : standardBusinessDocumentHeader.getBusinessScope().getScope()) {
            if (scope.getType().equalsIgnoreCase("DOCUMENTID")) {
                String documentIdentifier = scope.getInstanceIdentifier();
                peppolMessageMetaData.setDocumentTypeIdentifier(PeppolDocumentTypeId.valueOf(documentIdentifier));
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
        return documentIdentification.getInstanceIdentifier();
    }

    private String getReceiver(StandardBusinessDocumentHeader standardBusinessDocumentHeader) {
        Partner partner = standardBusinessDocumentHeader.getReceiver().get(0);
        PartnerIdentification identifier = partner.getIdentifier();
        return identifier.getValue();
    }

    private Unmarshaller createUnmarshaller() {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to create JAXB unmarshaller: " + e.getMessage(), e);
        }
    }

    private XMLStreamReader createXmlStreamReader(InputStream inputStream) {
        try {
            return xmlInputFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to crate XML Stream Reader: " + e.getMessage(), e);
        }
    }

}

