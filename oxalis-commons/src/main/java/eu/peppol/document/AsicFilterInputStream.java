/*
 * Copyright (c) 2010 - 2016 Norwegian Agency for Public Government and eGovernment (Difi)
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

package eu.peppol.document;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * Extracts the Base64 encoded ASiC archive. Remember to Base64 decode the data before you use it.
 *
 * @author steinar
 *         Date: 11.01.2016
 *         Time: 15.47
 */
public class AsicFilterInputStream extends FilterInputStream {

    private XMLEventReader xmlEventReader;
    private boolean insideAsicElement;
    private ByteBuffer byteBuffer = null;

    public AsicFilterInputStream(InputStream inputStream) {
        super(inputStream);

        Path xmlFile = null;
        try {
            xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(super.in, "UTF-8");

            insideAsicElement = false;
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to initialize");
        }
    }

    @Override
    public int read() throws IOException {
        if (fillBufferIfNeeded() == -1) {
            return -1;
        } else
            return byteBuffer.get();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (fillBufferIfNeeded() == -1)
            return -1;

        int remaining = byteBuffer.remaining();
        int bytesToRead;
        if (len > remaining) {
            bytesToRead = remaining;
        } else {
            bytesToRead = len;
        }

        byteBuffer.get(b, off, bytesToRead);
        return bytesToRead;
    }

    @Override
    public long skip(long n) throws IOException {
        throw new IllegalStateException("You may not skip anything when extracting ASiC archives from XML file");
    }

    @Override
    public int available() throws IOException {
        if (fillBufferIfNeeded() == -1) {
            return 0;
        } else
            return byteBuffer.remaining();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public synchronized void mark(int readlimit) {

    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("Mark/reset not supported.");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    protected int fillBufferIfNeeded() {

        if (byteBuffer != null && byteBuffer.remaining() > 0)
            return byteBuffer.remaining();

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = null;
            try {
                xmlEvent = xmlEventReader.nextEvent();
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Unable to read another XML event. " + e.getMessage(), e);
            }

            switch (xmlEvent.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    String localPart = xmlEvent.asStartElement().getName().getLocalPart();
                    if ("asic".equals(localPart)) {
                        insideAsicElement = true;
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    localPart = xmlEvent.asEndElement().getName().getLocalPart();
                    if ("asic".equals(localPart)) {
                        insideAsicElement = false;
                    }
                    break;


                case XMLEvent.CHARACTERS:
                    // Whenever we are inside the ASiC XML element, spit
                    // out the base64 encoded data into the base64 decoding output stream.
                    if (insideAsicElement) {
                        Characters characters = xmlEvent.asCharacters();
                        try {
                            byte[] bytes1 = characters.getData().getBytes("UTF-8");
                            byteBuffer = ByteBuffer.wrap(bytes1).asReadOnlyBuffer();

                            return bytes1.length;

                        } catch (UnsupportedEncodingException e) {
                            throw new IllegalStateException("Unable to convert string to bytes " + e.getMessage(), e);
                        }
                    }
                    break;
            }
        }

        return -1;
    }
}
