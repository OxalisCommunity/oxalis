package network.oxalis.commons.tag;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import network.oxalis.api.settings.Settings;
import network.oxalis.api.tag.TagGenerator;
import network.oxalis.commons.guice.ImplLoader;
import network.oxalis.commons.guice.OxalisModule;

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
