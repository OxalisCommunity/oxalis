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

package network.oxalis.commons.header;

import com.google.inject.Singleton;
import network.oxalis.api.header.HeaderParser;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.util.Type;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.sbdh.SbdReader;
import network.oxalis.vefa.peppol.sbdh.lang.SbdhException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.2
 */
@Type("sbdh")
@Singleton
public class SbdhHeaderParser implements HeaderParser {

    /**
     * Simple wrapper around peppol-sbdh module.
     *
     * @param inputStream the inputstream containing the XML
     * @return an instance of Header if found, otherwise null.
     */
    @Override
    public Header parse(InputStream inputStream) throws OxalisContentException {
        try (SbdReader sbdReader = SbdReader.newInstance(inputStream)) {
            return sbdReader.getHeader();
        } catch (SbdhException | IOException e) {
            throw new OxalisContentException(e.getMessage(), e);
        }
    }
}
