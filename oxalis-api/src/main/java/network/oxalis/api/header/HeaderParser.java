package network.oxalis.api.header;

import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.vefa.peppol.common.model.Header;

import java.io.InputStream;

/**
 * @author erlend
 * @since 4.0.2
 */
public interface HeaderParser {

    Header parse(InputStream inputStream) throws OxalisContentException;

}
