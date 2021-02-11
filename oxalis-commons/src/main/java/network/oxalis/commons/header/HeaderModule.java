package network.oxalis.commons.header;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import network.oxalis.api.header.HeaderParser;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.ImplLoader;
import network.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 * @since 4.0.2
 */
public class HeaderModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(HeaderParser.class, SbdhHeaderParser.class);

        bindSettings(HeaderConf.class);
    }

    @Provides
    @Singleton
    protected HeaderParser getHeaderParser(Injector injector, Settings<HeaderConf> settings) {
        return ImplLoader.get(injector, HeaderParser.class, settings, HeaderConf.PARSER);
    }
}
