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

package network.oxalis.outbound.transformer;

import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.transformer.ContentWrapper;
import network.oxalis.api.util.Type;
import network.oxalis.vefa.peppol.common.model.Header;
import network.oxalis.vefa.peppol.sbdh.SbdWriter;
import network.oxalis.vefa.peppol.sbdh.lang.SbdhException;
import network.oxalis.vefa.peppol.sbdh.util.XMLStreamUtils;

import javax.inject.Singleton;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.1
 */
@Singleton
@Type("xml")
public class XmlContentWrapper implements ContentWrapper {

    @Override
    public InputStream wrap(InputStream inputStream, Header header) throws IOException, OxalisContentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (SbdWriter sbdWriter = SbdWriter.newInstance(outputStream, header)) {
            XMLStreamUtils.copy(inputStream, sbdWriter.xmlWriter());
        } catch (SbdhException | XMLStreamException e) {
            throw new OxalisContentException("Unable to wrap content into SBDH.", e);
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
