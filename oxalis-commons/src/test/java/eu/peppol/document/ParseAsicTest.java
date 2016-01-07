/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Public Government and eGovernment (Difi)
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

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.xml.stream.*;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.IllegalSelectorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.Assert.assertNotNull;

/**
 * Parses an SBD containing SBDH + base64 encoded ASiC archive.
 * <p>
 * The ASiC archive is decoded and separated into a separate file.
 *
 * @author steinar
 *         Date: 23.12.2015
 *         Time: 16.37
 */
public class ParseAsicTest {

    public static final Logger log = LoggerFactory.getLogger(ParseAsicTest.class);

    /**
     * Takes a file holding an SBD/SBDH with an ASiC archive in base64 as payload and extracts the ASiC archive in binary format, while
     * calculating the message digest.
     *
     * @throws Exception
     */
    @Test
    public void parseSampleSbdWithAsic() throws Exception {
        InputStream resourceAsStream = ParseAsicTest.class.getClassLoader().getResourceAsStream("sample-sbd-with-asic.xml");

        AsicExtractor asicExtractor = new AsicExtractor(resourceAsStream);

        Path asicFile = Files.createTempFile("unit-test", ".asice");
        OutputStream asicOutputStream = Files.newOutputStream(asicFile);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        DigestOutputStream digestOutputStream = new DigestOutputStream(asicOutputStream, messageDigest);
        Base64OutputStream base64OutputStream = new Base64OutputStream(digestOutputStream, false);

        IOUtils.copy(asicExtractor, base64OutputStream);
        log.debug("Wrote ASiC to:" + asicFile);
        log.debug("Digest: " + new String(Base64.getEncoder().encode(messageDigest.digest())));

    }

    public static class AsicExtractor extends FilterInputStream {

        private XMLEventReader xmlEventReader;
        private boolean insideAsicElement;
        private ByteBuffer byteBuffer = null;

        public AsicExtractor(InputStream inputStream) {
            super(inputStream);


            Path xmlFile = null;
            try {
                xmlFile = Files.createTempFile("unit-test", ".xml");
                xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(super.in, "UTF-8");
                FileOutputStream outputStream = new FileOutputStream(xmlFile.toFile());
                XMLEventWriter xmlEventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(outputStream, "UTF-8");

                insideAsicElement = false;
            } catch (XMLStreamException | IOException e) {
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
                                byteBuffer = ByteBuffer.wrap(bytes1);

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
}
