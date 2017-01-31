/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.commons.sbdh;

import eu.peppol.PeppolStandardBusinessHeader;
import no.difi.vefa.peppol.sbdh.SbdReader;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;

import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of SBDH parser, which is optimized for speed on large files.
 * <p>
 * It will first use a SAX parser to extract the <code>StandardBusinessDocumentHeader</code> only and
 * create a W3C DOM object.
 * <p>
 * The W3C Document is then fed into JAXB, which saves us all the hassle of using Xpath to extract the data.
 * <p>
 * This class is not thread safe.
 *
 * @author steinar
 *         Date: 24.06.15
 *         Time: 15.58
 * @author erlend
 */
public class SbdhFastParser {

    /**
     * Parses the inputstream from first occurence of &lt;StandardBusinessDocumentHeader&gt; to
     * the corresponding &lt;/StandardBusinessDocumentHeader&gt; into a W3C DOM object, after which the DOM
     * is unmarshalled into an Object graph using JaxB.
     * <p>
     * Not very pretty, but it improves speed a lot when you have large XML documents.
     *
     * @param inputStream the inputstream containing the XML
     * @return an instance of PeppolStandardBusinessHeader if found, otherwise null.
     */
    public static PeppolStandardBusinessHeader parse(InputStream inputStream) {
        if (inputStream.markSupported())
            inputStream.mark(1024 * 16);

        PeppolStandardBusinessHeader result;
        try (SbdReader sbdReader = SbdReader.newInstance(inputStream)) {
            result = new PeppolStandardBusinessHeader(sbdReader.getHeader());
        } catch (SbdhException | IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        try {
            if (inputStream.markSupported())
                inputStream.reset();
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to reset intput stream: %s", e.getMessage()), e);
        }

        return result;
    }
}
