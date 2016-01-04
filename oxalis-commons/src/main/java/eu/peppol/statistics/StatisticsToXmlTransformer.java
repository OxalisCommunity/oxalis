package eu.peppol.statistics;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.Date;

/**
 * User: steinar
 * Date: 24.02.13
 * Time: 10:36
 */
public class StatisticsToXmlTransformer implements StatisticsTransformer {


    private final OutputStream outputStream;
    private XMLStreamWriter xmlStreamWriter;

    public StatisticsToXmlTransformer(OutputStream outputStream) {

        if (outputStream == null) {
            throw new IllegalArgumentException("Required argument outputStream is null");
        }
        this.outputStream = outputStream;
    }

    @Override
    public void startStatistics(Date start, Date end) {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

        try {
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream, "UTF-8");
            xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
            // TODO: consider the use of a name space?
            xmlStreamWriter.writeStartElement(STATISTICS_DOCUMENT_START_ELEMENT_NAME);

            String startString = String.format("%tF %tR", start, start);
            xmlStreamWriter.writeAttribute("start", startString);

            String endString = String.format("%tF %tR", end, end);
            xmlStreamWriter.writeAttribute("end", endString);

        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to create XML stream writer; " + e.getMessage(), e);
        }
    }

    @Override
    public void startEntry() {

        try {
            xmlStreamWriter.writeStartElement(ENTRY_START_ELEMENT_NAME);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to write element " + STATISTICS_DOCUMENT_START_ELEMENT_NAME + "; " + e, e);
        }
    }

    @Override
    public void writeAccessPointIdentifier(String accessPointIdentifier) {
        writeElementAndContents(ACCESS_POINT_ID_ELEMENT_NAME,accessPointIdentifier);
    }

    @Override
    public void writePeriod(String period) {
        writeElementAndContents(PERIOD_ELEMENT_NAME, period);
    }

    @Override
    public void writeDirection(String direction) {
        writeElementAndContents(DIRECTION_ELEMENT_NAME, direction);
    }

    private void writeElementAndContents(String elementName, String contents) {
        try {
            xmlStreamWriter.writeStartElement(elementName);
            if (contents != null){
                xmlStreamWriter.writeCharacters(contents);
            }
            xmlStreamWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to write " + elementName + " element with value of " + contents + "; " + e, e);
        }
    }

    @Override
    public void writeParticipantIdentifier(String participantId) {
        writeElementAndContents(PARTICIPANT_ID_ELEMENT_NAME, participantId);
    }

    @Override
    public void writeDocumentType(String documentType) {
        writeElementAndContents(DOCUMENT_TYPE_ELEMENT_NAME, documentType);
    }

    @Override
    public void writeProfileId(String profileId) {
        writeElementAndContents(PROFILE_ID_ELEMENT_NAME, profileId);
    }

    @Override
    public void writeChannel(String channel) {
        writeElementAndContents(CHANNEL_ELEMENT_NAME, channel);
    }

    @Override
    public void writeCount(int count) {
        writeElementAndContents(COUNT_ELEMENT_NAME, ""+count);
    }

    @Override
    public void endEntry() {
        try {
            xmlStreamWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to write statistics end element " + e, e);
        }

    }

    @Override
    public void endStatistics() {

        try {
            xmlStreamWriter.writeEndDocument();
            xmlStreamWriter.flush();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to write end of statistics document; " + e.getMessage(), e);
        }
    }
}
