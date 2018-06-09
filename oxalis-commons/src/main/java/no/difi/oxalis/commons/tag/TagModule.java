package no.difi.oxalis.commons.tag;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.api.tag.TagGenerator;
import no.difi.oxalis.commons.guice.ImplLoader;
import no.difi.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 * @since 4.0.2
 */
public class TagModule extends OxalisModule {

    @Override
    protected void configure() {
        bindTyped(TagGenerator.class, NoopTagGenerator.class);

        bindSettings(TagConf.class);
    }

    @Provides
    @Singleton
    protected TagGenerator getTagGenerator(Injector injector, Settings<TagConf> settings) {
        return ImplLoader.get(injector, TagGenerator.class, settings, TagConf.GENERATOR);
    }
}
