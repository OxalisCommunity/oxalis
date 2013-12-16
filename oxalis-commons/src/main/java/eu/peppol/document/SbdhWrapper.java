package eu.peppol.document;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.SchemeId;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Takes a document without an SBDH and wraps it with an SBDH.
 *
 * This code was written as an experimental use of STaX and could probably be cleaned up. You are welcome to do so :-)
 */
public class SbdhWrapper {


    private static final String SBDH_URI = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader";
    private final Characters tab;
    private ParticipantId receiver;
    private ParticipantId sender;
    private final XMLEventFactory eventFactory;
    private String namespaceURI;
    private String ublVersion;
    private final Characters nl;
    private String localName;
    private String customizationId;
    private String profileID;
    private final Stack<String> currentContext;
    private List<XMLEvent> bufferedEvents;
    private XMLEventWriter xmlEventWriter;

    public SbdhWrapper() {

        eventFactory = XMLEventFactory.newFactory();
        // Creates a constant for the newline character
        nl = eventFactory.createCharacters("\n");

        // Creates a constant for TAB character
        tab = eventFactory.createCharacters("\t");

        currentContext = new Stack<String>();
    }


    /**
     * Wraps the XML document supplied in the InputStream in a SBDH.
     *
     * First we read the inputstream, looking for the various identifiers to be used in the SBDH. However; rather than writing the XML to the output
     * we buffer the output until all the data needed for the SBDH has been collected, after which we:
     * <ol>
     *     <li>Write the SBDH with the data obtained from parsing the first part of the XML document</li>
     *     <li>Flush the XML events parsed to obtain the SBDH information.</li>
     *     <li>Read and write the rest of the XML input</li>
     * </ol>
     * @param inputStream
     * @return
     */
    public byte[] wrap(InputStream inputStream) {

        // We start off by buffering all the XML events read from the input source
        boolean bufferTheEvents = true;

        // and this is where we buffer the events
        bufferedEvents = new ArrayList<XMLEvent>();

        // This is where we place the resulting output
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            XMLEventReader xmlEventReader = XMLInputFactory.newFactory().createXMLEventReader(inputStream);
            xmlEventWriter = XMLOutputFactory.newFactory().createXMLEventWriter(byteArrayOutputStream);

            boolean startElementSeen = false;

            // Parses the very first element in the XML document
            parseFirstElement(xmlEventReader);

            // Iterates the XML events until we have read the entire input stream
            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();

                saveCurrentXPath(currentContext, xmlEvent);

                if (bufferTheEvents) {

                    // Don't buffer XML Processing instructions
                    if (xmlEvent.isProcessingInstruction()) {
                        continue;
                    }
                    // Ignores the <?xml version= ... at the start
                    if (xmlEvent.isStartDocument()) {
                        continue;
                    }

                    // Saves this event into our buffer
                    bufferedEvents.add(xmlEvent);

                } else {
                    // If we have finished buffering, just emit the XML event to the output
                    xmlEventWriter.add(xmlEvent);
                }

                // Parses the local name and the name space URI for the start element
                if (bufferTheEvents && xmlEvent.isStartElement() && !startElementSeen) {
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


                // Now look for the CompanyID within a PartyLegalEntity
                if (bufferTheEvents && xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("CompanyID")) {

                    XMLEvent textEvent = xmlEventReader.nextEvent();

                    // We only care about CompanyID if they are within a PartyLegalEntity, for which the CompanyID is mandatory
                    if (currentContext.search("PartyLegalEntity") >= 1) {

                        // The actual company identification is held in the text
                        String companyID = textEvent.asCharacters().getData().trim();
                        // .... while the schemeID is held in the attribute
                        String schemeIDAsText = xmlEvent.asStartElement().getAttributeByName(new QName("schemeID")).getValue().trim();

                        // Parses the schemeID in order to determine the correct prefix in front of the company ID
                        SchemeId schemeId = SchemeId.parse(schemeIDAsText);

                        ParticipantId participantId = new ParticipantId(schemeId.getIso6523Icd() + ":" + companyID);

                        // figures out whether the legal entity is the receiver or the sender.
                        if (currentContext.search("AccountingCustomerParty") >= 1) {

                            receiver = participantId;
                        } else if (currentContext.search("AccountingSupplierParty") >= 1) {
                            sender = participantId;
                        }

                        // Ah, finally. We have the last bits of the SBDH information now emit the SBDH and flush the buffered XML events.
                        if (receiver != null && sender != null) {
                            // Stop buffering the events from now on
                            bufferTheEvents = false;

                            // Emits the SBDH
                            emitSBDH(xmlEventWriter);

                            // Empties the buffer of cached XML events.
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

            saveCurrentXPath(currentContext, xmlEvent);

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


    /**
     * Emits the SBDH via the supplied XMLEventWriter.
     *
     * @param xmlEventWriter
     * @throws XMLStreamException
     */
    private void emitSBDH(XMLEventWriter xmlEventWriter) throws XMLStreamException {
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
    }

    private void flushBuffer(List<XMLEvent> bufferedEvents, XMLEventWriter xmlEventWriter) throws XMLStreamException {

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
