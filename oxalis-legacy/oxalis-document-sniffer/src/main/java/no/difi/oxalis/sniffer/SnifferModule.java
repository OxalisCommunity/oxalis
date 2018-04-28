package no.difi.oxalis.sniffer;

import no.difi.oxalis.api.transformer.ContentDetector;
import no.difi.oxalis.commons.guice.OxalisModule;
import no.difi.oxalis.sniffer.document.NoSbdhParser;

/**
 * @author erlend
 * @since 4.0.1
 */
public class SnifferModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(ContentDetector.class, NoSbdhParser.class);
    }
}
