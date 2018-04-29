package no.difi.oxalis.commons.transformer;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.api.transformer.ContentDetector;
import no.difi.oxalis.api.transformer.ContentWrapper;
import no.difi.oxalis.commons.guice.ImplLoader;
import no.difi.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 * @since 4.0.1
 */
public class TransformerModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(ContentDetector.class, NoopContentDetector.class);
        bindTyped(ContentWrapper.class, NoopContentWrapper.class);

        bindSettings(TransformerConf.class);
    }

    @Provides
    @Singleton
    protected ContentDetector getContentDetector(Injector injector, Settings<TransformerConf> settings) {
        return ImplLoader.get(injector, ContentDetector.class, settings, TransformerConf.DETECTOR);
    }

    @Provides
    @Singleton
    protected ContentWrapper getContentWrapper(Injector injector, Settings<TransformerConf> settings) {
        return ImplLoader.get(injector, ContentWrapper.class, settings, TransformerConf.WRAPPER);
    }
}
