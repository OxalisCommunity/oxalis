package no.difi.oxalis.sniffer;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import no.difi.oxalis.api.transformer.ContentDetector;
import no.difi.oxalis.sniffer.document.NoSbdhParser;

/**
 * @author erlend
 * @since 4.0.1
 */
public class SnifferModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(ContentDetector.class, Names.named("legacy"))).to(NoSbdhParser.class).in(Singleton.class);
    }
}
