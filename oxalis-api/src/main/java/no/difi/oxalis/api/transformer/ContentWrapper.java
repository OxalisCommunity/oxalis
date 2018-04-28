package no.difi.oxalis.api.transformer;

import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.1
 */
public interface ContentWrapper {

    InputStream wrap(InputStream inputStream, Header header) throws IOException, OxalisContentException;

}
