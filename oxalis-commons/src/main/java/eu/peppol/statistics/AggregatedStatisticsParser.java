package eu.peppol.statistics;

import eu.peppol.start.identifier.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import static eu.peppol.statistics.StatisticsTransformer.*;

/**
 * @author steinar
 *         Date: 25.03.13
 *         Time: 14:57
 */
public class AggregatedStatisticsParser {

    private final XMLInputFactory xmlInputFactory;

    public AggregatedStatisticsParser() {

        xmlInputFactory = XMLInputFactory.newInstance();
    }

    public Collection<AggregatedStatistics> parse(InputStream inputStream) {

        Collection<AggregatedStatistics> result = new ArrayList<AggregatedStatistics>();

        XMLEventReader xmlEventReader = createXmlEventReader(inputStream);

        XMLEvent xmlEvent;
        AggregatedStatistics.Builder builder = null;

        DateTimeFormatter dateTimeParser = ISODateTimeFormat.dateOptionalTimeParser();

        while (xmlEventReader.hasNext()) {
            try {
                 xmlEvent = xmlEventReader.nextEvent();

                if (xmlEvent.isStartElement()) {

                    String localName = xmlEvent.asStartElement().getName().getLocalPart();
                    if (localName.equals(ENTRY_START_ELEMENT_NAME)) {
                        builder = new AggregatedStatistics.Builder();
                        continue;
                    }

                    // All elements contains character data
                    Characters characters = xmlEventReader.nextEvent().asCharacters();

                    if (localName.equals(ACCESS_POINT_ID_ELEMENT_NAME)) {
                        builder.accessPointIdentifier(new AccessPointIdentifier(characters.getData()));
                        continue;
                    }

                    if (localName.equals(DIRECTION_ELEMENT_NAME)) {
                        builder.direction(Direction.valueOf(characters.getData().toUpperCase()));
                        continue;
                    }

                    if (localName.equals(PERIOD_ELEMENT_NAME)) {
                        DateTime dateTime = dateTimeParser.parseDateTime(characters.getData());
                        builder.date(dateTime.toDate());
                        continue;

                    }
                    if (localName.equals(PARTICIPANT_ID_ELEMENT_NAME)) {
                        builder.participantId(new ParticipantId(characters.getData()));
                        continue;
                    }

                    if (localName.equals(CHANNEL_ELEMENT_NAME)) {
                        builder.channel(new ChannelId(characters.getData()));
                        continue;
                    }

                    if (localName.equals(DOCUMENT_TYPE_ELEMENT_NAME)) {
                        builder.documentType(PeppolDocumentTypeId.valueOf(characters.getData()));
                        continue;
                    }

                    if (localName.equals(PROFILE_ID_ELEMENT_NAME)) {
                        builder.profile(PeppolProcessTypeId.valueOf(characters.getData()));
                        continue;
                    }

                    if (localName.equals(COUNT_ELEMENT_NAME)) {
                        builder.count(Integer.parseInt(characters.getData()));
                        continue;
                    }
                }

                if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals(ENTRY_START_ELEMENT_NAME)) {
                    AggregatedStatistics aggregatedStatistics = builder.build();
                    result.add(aggregatedStatistics);
                }
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Unable parse xml from input stream: " + e, e);
            }
        }

        return result;

    }

    private XMLEventReader createXmlEventReader(InputStream inputStream) {
        XMLEventReader xmlEventReader;
        try {
            xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream, "UTF-8");
            return xmlEventReader;
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to create xml event reader for stream " + e, e);
        }
    }
}
