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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.Assert.assertNotNull;

/**
 * Parses an SBD containing SBDH + base64 encoded ASiC archive.
 *
 * The ASiC archive is decoded and separated into a separate file.
 *
 * @author steinar
 *         Date: 23.12.2015
 *         Time: 16.37
 */
public class PayloadParserTest {

    public static final Logger log = LoggerFactory.getLogger(PayloadParserTest.class);

    /**
     * Takes a file holding an SBD/SBDH with an ASiC archive in base64 as payload and extracts the ASiC archive in binary format, while
     * calculating the message digest.
     *
     * @throws Exception
     */
    @Test
    public void parseSampleSbdWithAsic() throws Exception {

        InputStream resourceAsStream = PayloadParserTest.class.getClassLoader().getResourceAsStream("sample-sbd-with-asic.xml");
        assertNotNull(resourceAsStream);

        Path xmlFile = Files.createTempFile("unit-test", ".xml");

        XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(resourceAsStream, "UTF-8");
        FileOutputStream outputStream = new FileOutputStream(xmlFile.toFile());
        XMLEventWriter xmlEventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(outputStream, "UTF-8");

        Path asicFile = Files.createTempFile("unit-test", ".asice");
        OutputStream asicOutputStream = Files.newOutputStream(asicFile);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        DigestOutputStream digestOutputStream = new DigestOutputStream(asicOutputStream, messageDigest);
        Base64OutputStream base64OutputStream = new Base64OutputStream(digestOutputStream, false);

        boolean insideAsicElement = false;

        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();

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
                        base64OutputStream.write(characters.getData().getBytes("UTF-8"));
                    }
                    break;
            }
            xmlEventWriter.add(xmlEvent);
        }

        asicOutputStream.close();
        outputStream.close();
        log.debug("Wrote xml output to: " + xmlFile);
        log.debug("Wrote ASiC to:" + asicFile);
        log.debug("Digest: " + new String(Base64.getEncoder().encode(messageDigest.digest())));
    }
}
