/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
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

import no.difi.vefa.peppol.common.model.Header;
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
public class SbdhParser {

    /**
     * Simple wrapper around peppol-sbdh module.
     *
     * @param inputStream the inputstream containing the XML
     * @return an instance of Header if found, otherwise null.
     */
    public static Header parse(InputStream inputStream) {
        try (SbdReader sbdReader = SbdReader.newInstance(inputStream)) {
            return sbdReader.getHeader();
        } catch (SbdhException | IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
