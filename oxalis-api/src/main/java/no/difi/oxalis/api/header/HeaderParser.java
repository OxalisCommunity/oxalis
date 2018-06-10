package no.difi.oxalis.api.header;

import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.vefa.peppol.common.model.Header;

import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.2
 */
public interface HeaderParser {

    Header parse(InputStream inputStream) throws OxalisContentException;

}
