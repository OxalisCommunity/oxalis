package no.difi.oxalis.commons.transformer;

import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.transformer.ContentDetector;
import no.difi.vefa.peppol.common.model.Header;

import javax.inject.Singleton;
import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.1
 */
@Singleton
public class NoopContentDetector implements ContentDetector {

    @Override
    public Header parse(InputStream inputStream) throws OxalisContentException {
        throw new OxalisContentException("Content does not contain SBDH.");
    }
}
