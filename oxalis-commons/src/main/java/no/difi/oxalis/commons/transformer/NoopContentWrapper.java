package no.difi.oxalis.commons.transformer;

import com.google.inject.Singleton;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.transformer.ContentWrapper;
import no.difi.oxalis.api.util.Type;
import no.difi.vefa.peppol.common.model.Header;

import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.1
 */
@Singleton
@Type("noop")
public class NoopContentWrapper implements ContentWrapper {

    @Override
    public InputStream wrap(InputStream inputStream, Header header) throws OxalisContentException {
        throw new OxalisContentException("No content wrapper is available.");
    }
}
