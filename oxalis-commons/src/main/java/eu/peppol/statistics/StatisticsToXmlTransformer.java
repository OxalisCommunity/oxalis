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
