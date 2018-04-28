package no.difi.oxalis.commons.transformer;

import com.google.inject.*;
import com.google.inject.name.Names;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.api.transformer.ContentDetector;
import no.difi.oxalis.api.transformer.ContentWrapper;
import no.difi.oxalis.commons.settings.SettingsBuilder;

/**
 * @author erlend
 * @since 4.0.1
 */
public class TransformerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Key.get(ContentDetector.class, Names.named("noop"))).to(NoopContentDetector.class);
        bind(Key.get(ContentWrapper.class, Names.named("noop"))).to(NoopContentWrapper.class);

        SettingsBuilder.with(binder(), TransformerConf.class);
    }

    @Provides
    @Singleton
    protected ContentDetector getContentDetector(Injector injector, Settings<TransformerConf> settings) {
        return injector.getInstance(
                Key.get(ContentDetector.class, settings.getNamed(TransformerConf.DETECTOR)));
    }

    @Provides
    @Singleton
    protected ContentWrapper getContentWrapper(Injector injector, Settings<TransformerConf> settings) {
        return injector.getInstance(
                Key.get(ContentWrapper.class, settings.getNamed(TransformerConf.WRAPPER)));
    }
}
