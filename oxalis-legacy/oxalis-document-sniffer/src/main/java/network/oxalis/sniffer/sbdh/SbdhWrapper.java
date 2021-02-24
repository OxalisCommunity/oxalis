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

package network.oxalis.sniffer.sbdh;

import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.sbdh.SbdWriter;
import network.oxalis.vefa.peppol.sbdh.util.XMLStreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Takes a document and wraps it together with headers into a StandardBusinessDocument.
 * <p>
 * The SBDH part of the document is constructed from the headers.
 * The document will be the payload (xs:any) following the SBDH.
 *
 * @author thore
 * @author steinar
 * @author erlend
 */
public class SbdhWrapper {

    /**
     * Wraps payload + headers into a StandardBusinessDocument
     *
     * @param inputStream the input stream to be wrapped
     * @param headers     the headers to use for sbdh
     * @return byte buffer with the resulting output in utf-8
     */
    public byte[] wrap(InputStream inputStream, Header headers) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (SbdWriter sbdWriter = SbdWriter.newInstance(baos, headers)) {
            XMLStreamUtils.copy(inputStream, sbdWriter.xmlWriter());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to wrap document inside SBD (SBDH). " + ex.getMessage(), ex);
        }

        return baos.toByteArray();
    }
}
