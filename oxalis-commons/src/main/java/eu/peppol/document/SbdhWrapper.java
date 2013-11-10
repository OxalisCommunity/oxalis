package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author steinar
 *         Date: 08.11.13
 *         Time: 15:50
 */
public class SbdhWrapper {


    public static final String SBDH_URI = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader";
    private final Characters tab;
    private boolean accountingSupplierPartySeen = false;
    private boolean accountingCustomerPartySeen = false;
    private ParticipantId receiver;
    private ParticipantId sender;
    private XMLEventFactory eventFactory;
    private String namespaceURI;
    private String ublVersion;
    private Characters nl;
    private String localName;
    private String customizationId;
    private String profileID;
    private Stack<String> ctx;
    private List<XMLEvent> bufferedEvents;
    private XMLEventReader xmlEventReader;
    private XMLEventWriter xmlEventWriter;

    public SbdhWrapper() {
        eventFactory = XMLEventFactory.newFactory();
        nl = eventFactory.createCharacters("\n");
        tab = eventFactory.createCharacters("\t");

        ctx = new Stack<String>();
    }

    public byte[] wrap(InputStream inputStream, PeppolStandardBusinessHeader peppolStandardBusinessHeader) {

        boolean bufferTheEvents = true;

        bufferedEvents = new ArrayList<XMLEvent>();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            xmlEventReader = XMLInputFactory.newFactory().createXMLEventReader(inputStream);
            xmlEventWriter = XMLOutputFactory.newFactory().createXMLEventWriter(byteArrayOutputStream);


            boolean startElementSeen = false;

            parseFirstElement(xmlEventReader);


            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();

                saveCurrentXPath(ctx, xmlEvent);

                if (bufferTheEvents) {

                    // Don't buffer XML Processing instructions
                    if (xmlEvent.isProcessingInstruction()) {
                        continue;
                    }
                    // Ignores the <?xml version= ... at the start
                    if (xmlEvent.isStartDocument()) {
                        continue;
                    }

                    bufferedEvents.add(xmlEvent);
                } else {
                    xmlEventWriter.add(xmlEvent);
                }

                if (bufferTheEvents && xmlEvent.isStartElement() && startElementSeen != true) {
                    startElementSeen = true;
                    QName rootName = xmlEvent.asStartElement().getName();
                    localName = rootName.getLocalPart();
                    namespaceURI = xmlEvent.asStartElement().getNamespaceURI(rootName.getPrefix());
                }

                if (bufferTheEvents && xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("UBLVersionID")) {
                    XMLEvent textEvent = xmlEventReader.nextEvent();
                    bufferedEvents.add(textEvent);
                    ublVersion = textEvent.asCharacters().getData().trim();
                }

                if (bufferTheEvents && xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("CustomizationID")) {
                    XMLEvent textEvent = xmlEventReader.nextEvent();
                    bufferedEvents.add(textEvent);
                    customizationId = textEvent.asCharacters().getData().trim();
                }
                if (bufferTheEvents && xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("ProfileID")) {
                    XMLEvent textEvent = xmlEventReader.nextEvent();
                    bufferedEvents.add(textEvent);
                    profileID = textEvent.asCharacters().getData().trim();
                }


                if (bufferTheEvents && xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("CompanyID")) {


                    XMLEvent textEvent = xmlEventReader.nextEvent();
                    if (ctx.search("PartyLegalEntity") >= 1) {

                        String companyID = textEvent.asCharacters().getData().trim();
                        String schemeIDAsText = xmlEvent.asStartElement().getAttributeByName(new QName("schemeID")).getValue().trim();

                        SchemeId schemeId = SchemeId.parse(schemeIDAsText);

                        ParticipantId participantId = new ParticipantId(schemeId.getIso6523Icd() + ":" + companyID);
                        if (ctx.search("AccountingCustomerParty") >= 1) {

                            receiver = participantId;
                        } else if (ctx.search("AccountingSupplierParty") >= 1) {
                            sender = participantId;
                        }

                        if (receiver != null && sender != null) {
                            bufferTheEvents = false;

                            flushBuffer(bufferedEvents, xmlEventWriter);

                            // Writes the current text content into the buffer as we have not found everything we need yet.
                            xmlEventWriter.add(textEvent);
                        } else {
                            bufferedEvents.add(textEvent);
                        }
                    }

                }
            }

        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Parses the root element of the document and fetches the the xml local name and xml namespace
     *
     * @param xmlEventReader
     * @throws XMLStreamException
     */
    QName parseFirstElement(XMLEventReader xmlEventReader) throws XMLStreamException {

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

            saveCurrentXPath(ctx, xmlEvent);

            // Skips past any XML Processing instructions
            if (xmlEvent.isProcessingInstruction()) {
                continue;
            }
            // Skips the <?xml version= ... at the start
            if (xmlEvent.isStartDocument()) {
                continue;
            }

            bufferedEvents.add(xmlEvent);

            if (xmlEvent.isStartElement()) {
                QName rootName = xmlEvent.asStartElement().getName();
                localName = rootName.getLocalPart();
                namespaceURI = rootName.getNamespaceURI();

                return rootName;
            }
        }

        throw new IllegalStateException("Internal error, iterated all elements without finding the root element");
    }

    private void flushBuffer(List<XMLEvent> bufferedEvents, XMLEventWriter xmlEventWriter) throws XMLStreamException {

        xmlEventWriter.add(eventFactory.createStartDocument());
        nl();
        StartElement standardBusinessDocument = eventFactory.createStartElement("", SBDH_URI, "StandardBusinessDocument");
        xmlEventWriter.add(standardBusinessDocument);
        Namespace namespace = eventFactory.createNamespace("http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader");
        xmlEventWriter.add(namespace);
        xmlEventWriter.add(nl);

        tab(1);
        StartElement standardBusinessDocumentHeader = eventFactory.createStartElement("", SBDH_URI, "StandardBusinessDocumentHeader");
        xmlEventWriter.add(standardBusinessDocumentHeader);
        xmlEventWriter.add(nl);

        tab(2);
        simpleElement(xmlEventWriter, "HeaderVersion", "1.0");
        tab(2);
        xmlEventWriter.add(eventFactory.createStartElement("", SBDH_URI, "Sender"));
        nl();
        tab(3);
        StartElement identifier = eventFactory.createStartElement("", SBDH_URI, "Identifier");
        xmlEventWriter.add(identifier);
        Attribute authorityAttribute = eventFactory.createAttribute("Authority", "iso6523-actorid-upis");
        xmlEventWriter.add(authorityAttribute);
        xmlEventWriter.add(eventFactory.createCharacters(sender.toString()));
        EndElement endIdentifier = eventFactory.createEndElement("", SBDH_URI, "Identifier");
        xmlEventWriter.add(endIdentifier);
        nl();

        tab(2);
        xmlEventWriter.add(eventFactory.createEndElement("", SBDH_URI, "Sender"));
        xmlEventWriter.add(nl);
        tab(2);
        xmlEventWriter.add(eventFactory.createStartElement("", SBDH_URI, "Receiver"));
        nl();
        tab(3);
        xmlEventWriter.add(identifier);
        xmlEventWriter.add(authorityAttribute);
        xmlEventWriter.add(eventFactory.createCharacters(receiver.toString()));
        xmlEventWriter.add(endIdentifier);
        nl();
        tab(2);
        xmlEventWriter.add(eventFactory.createEndElement("", SBDH_URI, "Receiver"));
        xmlEventWriter.add(nl);

        tab(2);
        xmlEventWriter.add(eventFactory.createStartElement("", SBDH_URI, "DocumentIdentification"));
        nl();
        tab(3);
        simpleElement(xmlEventWriter, "Standard", namespaceURI);
        tab(3);
        simpleElement(xmlEventWriter, "TypeVersion", ublVersion);

        tab(3);
        simpleElement(xmlEventWriter, "InstanceIdentifier", UUID.randomUUID().toString());
        tab(3);
        simpleElement(xmlEventWriter, "Type", localName);
        tab(3);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleElement(xmlEventWriter, "CreationDateAndTime", simpleDateFormat.format(new Date()));
        tab(2);
        xmlEventWriter.add(eventFactory.createEndElement("", SBDH_URI, "DocumentIdentification"));
        nl();
        tab(2);
        xmlEventWriter.add(eventFactory.createStartElement("", SBDH_URI, "BusinessScope"));
        nl();
        tab(3);
        xmlEventWriter.add(eventFactory.createStartElement("", SBDH_URI, "Scope"));
        nl();
        tab(4);
        simpleElement(xmlEventWriter, "Type", "DOCUMENTID");
        tab(4);
        simpleElement(xmlEventWriter, "InstanceIdentifier", customizationId);
        tab(3);
        xmlEventWriter.add(eventFactory.createEndElement("", SBDH_URI, "Scope"));
        nl();

        tab(3);
        xmlEventWriter.add(eventFactory.createStartElement("", SBDH_URI, "Scope"));
        nl();
        tab(4);
        simpleElement(xmlEventWriter, "Type", "PROCESSID");
        tab(4);
        simpleElement(xmlEventWriter, "InstanceIdentifier", profileID);
        tab(3);
        xmlEventWriter.add(eventFactory.createEndElement("", SBDH_URI, "Scope"));
        xmlEventWriter.add(nl);
        tab(2);
        xmlEventWriter.add(eventFactory.createEndElement("", SBDH_URI, "BusinessScope"));
        xmlEventWriter.add(nl);
        tab(1);
        xmlEventWriter.add(eventFactory.createEndElement("", SBDH_URI, "StandardBusinessDocumentHeader"));
        xmlEventWriter.add(nl);

        // Flush the buffer now
        for (XMLEvent event : bufferedEvents) {
            xmlEventWriter.add(event);
        }

    }

    private void nl() throws XMLStreamException {
        xmlEventWriter.add(nl);
    }

    private void tab(int count) throws XMLStreamException {
        for (int i= 0; i < count; i++) {
            xmlEventWriter.add(tab);
        }
    }

    private void simpleElement(XMLEventWriter xmlEventWriter, String tagName, String value) throws XMLStreamException {
        xmlEventWriter.add(eventFactory.createStartElement("", SBDH_URI, tagName));
        xmlEventWriter.add(eventFactory.createCharacters(value));
        xmlEventWriter.add(eventFactory.createEndElement("", SBDH_URI, tagName));
        xmlEventWriter.add(nl);
    }

    private void saveCurrentXPath(Stack<String> ctx, XMLEvent xmlEvent) {
        if (xmlEvent.isStartElement()) {
            ctx.push(xmlEvent.asStartElement().getName().getLocalPart());
        }

        if (xmlEvent.isEndElement()) {
            ctx.pop();
        }
    }

    public ParticipantId getReceiver() {
        return receiver;
    }

    public ParticipantId getSender() {
        return sender;
    }
}
