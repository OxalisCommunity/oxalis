package no.difi.oxalis.outbound.transformer;

import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.transformer.ContentWrapper;
import no.difi.oxalis.api.util.Type;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.sbdh.SbdWriter;
import no.difi.vefa.peppol.sbdh.lang.SbdhException;
import no.difi.vefa.peppol.sbdh.util.XMLStreamUtils;

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
